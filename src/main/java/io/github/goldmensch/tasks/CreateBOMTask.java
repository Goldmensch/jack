package io.github.goldmensch.tasks;

import io.github.goldmensch.config.project.Project;
import io.github.goldmensch.AbortProgramException;
import io.github.goldmensch.Jack;
import io.github.goldmensch.bom.AddingDependencyVisitor;
import io.github.goldmensch.bom.BomUtil;
import org.cyclonedx.Version;
import org.cyclonedx.exception.ParseException;
import org.cyclonedx.generators.json.BomJsonGenerator;
import org.cyclonedx.model.*;
import org.cyclonedx.parsers.JsonParser;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.DefaultFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class CreateBOMTask extends Task {


    private static final Logger log = LoggerFactory.getLogger(CreateBOMTask.class);

    private final Project project;
    private final Path sbomPath;


    public CreateBOMTask(Jack jack) {
        super(jack, TaskType.DEPENDENCIES);
        this.project = jack.projectConfig().project();
        this.sbomPath = jack.paths().sbom();
    }

    @Override
    protected void run() {
        DependenciesTask dependenciesTask = dependency(TaskType.DEPENDENCIES);
        DependencyNode rootNode = dependenciesTask.dependencyResult().getRoot();

        var bom = createBom();

        bom.setComponents(resolveComponents(rootNode));
        bom.setDependencies(resolveDependencies(rootNode));
        bom.setMetadata(createMetadata());

        writeBom(bom);
    }

    private void writeBom(Bom bom) {
        try {
            String json = new BomJsonGenerator(bom, Version.VERSION_16).toJsonString();
            Files.deleteIfExists(sbomPath);
            Files.writeString(sbomPath, json, StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("I/O Exception while trying to recreate bom file at {}", sbomPath, e);
            throw new AbortProgramException();
        }
    }

    private Bom createBom() {
        try {
            return Files.exists(sbomPath)
                    ? new JsonParser().parse(Files.newInputStream(jack.paths().sbom()))
                    : new Bom();
        } catch (ParseException e) {
            log.error("Exception while parsing existing bom file at {}", sbomPath, e);
        } catch (IOException e) {
            log.error("I/O Exception while trying to read existing bom file at {}", sbomPath, e);
        }
        throw new AbortProgramException();
    }

    private Metadata createMetadata() {
        Metadata metadata = new Metadata();
        metadata.setComponent(createProjectComponent());
        metadata.setAuthors(authors());
        return metadata;
    }

    private List<OrganizationalContact> authors() {
        return project.authors()
                .stream()
                .map(name -> {
                    OrganizationalContact contact = new OrganizationalContact();
                    contact.setName(name);
                    return contact;
                })
                .toList();
    }

    private Component createProjectComponent() {
        Component component = new Component();
        component.setName(project.name());
        component.setVersion(project.version().versionString());
        return component;
    }

    private List<Component> resolveComponents(DependencyNode root) {
        return root.getChildren().stream()
                .map(DependencyNode::getArtifact)
                .map(this::componentFromArtifact)
                .toList();
    }

    private List<Dependency> resolveDependencies(DependencyNode root) {
        return root.getChildren()
                .stream()
                .map(this::dependencyFromDependencyNode)
                .toList();
    }

    private Dependency dependencyFromDependencyNode(DependencyNode dependencyNode) {
        Dependency dependency = new Dependency(BomUtil.purlFromArtifact(dependencyNode.getArtifact()).canonicalize());
        dependencyNode.accept(new AddingDependencyVisitor(dependency, dependencyNode));
        return dependency;
    }

    private Component componentFromArtifact(Artifact artifact) {
        Component component = new Component();
        component.setType(Component.Type.LIBRARY);
        component.setGroup(artifact.getGroupId());
        component.setName(artifact.getArtifactId());
        component.setVersion(artifact.getVersion());
        component.setPurl(BomUtil.purlFromArtifact(artifact));
        component.setHashes(retrieveHashes(artifact));
        component.setBomRef(component.getPurl());
        return component;
    }

    private List<Hash> retrieveHashes(Artifact artifact) {
        try {
            Path artifactPath = artifact.getFile().toPath();
            Path sha1HashPath = artifactPath.getParent().resolve(artifactPath.getFileName().toString() + ".sha1");
            String sha1Checksum = new DefaultFileProcessor().readChecksum(sha1HashPath.toFile());
            return List.of(new Hash("sha1", sha1Checksum));
        } catch (IOException e) {
            log.error("I/O Error while trying to read hash file from local maven cache for artifact {}", artifact, e);
            throw new AbortProgramException();
        }
    }
}
