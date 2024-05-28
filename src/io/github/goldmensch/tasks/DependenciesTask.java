package io.github.goldmensch.tasks;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.github.goldmensch.Jack;
import io.github.goldmensch.config.project.Project;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.cyclonedx.Version;
import org.cyclonedx.exception.ParseException;
import org.cyclonedx.generators.json.BomJsonGenerator;
import org.cyclonedx.model.*;
import org.cyclonedx.parsers.JsonParser;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.internal.impl.DefaultFileProcessor;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.visitor.DependencyGraphDumper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;

public final class DependenciesTask extends Task {

    public static final Logger log = LoggerFactory.getLogger(DependenciesTask.class);
    
    private static final RepositorySystemSupplier REPOSITORY_SYSTEM_SUPPLIER = new RepositorySystemSupplier();

    private final RepositorySystemSession session;
    private final RepositorySystem repositorySystem;
    private final List<RemoteRepository> resolutionRepositories;

    private Collection<Path> libraryJars;
    private DependencyResult dependencyResult;

    public DependenciesTask(Jack jack) {
        super(jack);
        this.repositorySystem = REPOSITORY_SYSTEM_SUPPLIER.get();
        var localRepository = new LocalRepository(jack.paths().mavenCache().toFile());

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
                .setSystemProperty("java.version", "21")
                .setConfigProperty("aether.trustedChecksumsSource.sparseDirectory", true)
                .setConfigProperty("aether.trustedChecksumsSource.basedir", "");

        List<RemoteRepository> repositories = jack.projectConfig().repositories().repositories()
                .stream()
                .map(repository -> new RemoteRepository.Builder(null, "default", repository.url()))
                .map(RemoteRepository.Builder::build)
                .toList();

        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
        this.resolutionRepositories = repositorySystem.newResolutionRepositories(session, repositories);
        this.session = session;
    }

    @Override
    protected void run() throws IOException, InterruptedException, ArtifactResolutionException, NoLocalRepositoryManagerException, DependencyCollectionException, DependencyResolutionException, ArtifactDescriptorException, ParseException {
        List<org.eclipse.aether.graph.Dependency> dependencies = jack.projectConfig().dependencies().dependencies()
                .stream()
                .map(dependency -> {
                    DefaultArtifact defaultArtifact = new DefaultArtifact(dependency.groupId(), dependency.artifactId(), "jar", dependency.version());
                    return new org.eclipse.aether.graph.Dependency(defaultArtifact, JavaScopes.COMPILE);
                })
                .toList();


        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRepositories(resolutionRepositories);
        collectRequest.setDependencies(dependencies);

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

        this.dependencyResult = repositorySystem.resolveDependencies(session, dependencyRequest);

        dependencyResult.getArtifactResults()
                .forEach(resultEntry -> log.info("Resolved dependency: {}", resultEntry));
        
        if (jack.providedArgs().contains("graph")) {
            DependencyGraphDumper dependencyGraphDumper = new DependencyGraphDumper(System.out::println);
            dependencyResult.getRoot().accept(dependencyGraphDumper);
        }

        libraryJars = dependencyResult.getArtifactResults()
                .stream()
                .map(artifactResult -> artifactResult.getArtifact().getFile().toPath())
                .toList();

        createSbom();
    }

    private void createSbom() throws IOException, ParseException {
        var bom = Files.exists(jack.paths().sbom())
                ? new JsonParser().parse(Files.newInputStream(jack.paths().sbom()))
                : new Bom();

        List<Component> components = dependencyResult.getRoot().getChildren().stream()
                .map(DependencyNode::getArtifact)
                .map(this::componentFromArtifact)
                .toList();
        bom.setComponents(components);

        List<Dependency> dependencies = dependencyResult.getRoot().getChildren()
                .stream()
                .map(this::dependencyFromDependencyNode)
                .toList();
        bom.setDependencies(dependencies);

        Component component = new Component();
        Project project = jack.projectConfig().project();
        component.setName(project.name());
        component.setVersion(project.version().versionString());

        List<OrganizationalContact> authors = project.authors()
                .stream()
                .map(name -> {
                    OrganizationalContact contact = new OrganizationalContact();
                    contact.setName(name);
                    return contact;
                })
                .toList();
        Metadata metadata = new Metadata();
        metadata.setComponent(component);
        metadata.setAuthors(authors);
        bom.setMetadata(metadata);


        String json = new BomJsonGenerator(bom, Version.VERSION_16).toJsonString();
        Files.delete(jack.paths().sbom());
        Files.writeString(jack.paths().sbom(), json, StandardOpenOption.CREATE);
    }

    private Dependency dependencyFromDependencyNode(DependencyNode dependencyNode) {
        Dependency dependency = new Dependency(purlFromArtifact(dependencyNode.getArtifact()).canonicalize());
        dependencyNode.accept(new AddingDependencyVisitor(dependency, dependencyNode));
        return dependency;
    }

    private class AddingDependencyVisitor implements DependencyVisitor {

        private final Dependency dependency;
        private final DependencyNode root;

        private AddingDependencyVisitor(Dependency dependency, DependencyNode root) {
            this.dependency = dependency;
            this.root = root;
        }

        @Override
        public boolean visitEnter(DependencyNode node) {
            if (root != node) {
                dependency.addDependency(new Dependency(purlFromArtifact(node.getArtifact()).canonicalize()));
            }
            return true;
        }

        @Override
        public boolean visitLeave(DependencyNode node) {
            return true;
        }
    }

    private PackageURL purlFromArtifact(Artifact artifact) {
        try {
            return new PackageURL("maven", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), null, null);
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Component componentFromArtifact(Artifact artifact) {
        Component component = new Component();
        component.setType(Component.Type.LIBRARY);
        component.setGroup(artifact.getGroupId());
        component.setName(artifact.getArtifactId());
        component.setVersion(artifact.getVersion());
        component.setPurl(purlFromArtifact(artifact));


        Path artifactPath = artifact.getFile().toPath();
        Path sha1HashPath = artifactPath.getParent().resolve(artifactPath.getFileName().toString() + ".sha1");
        String sha1Checksum = null;
        try {
            sha1Checksum = new DefaultFileProcessor().readChecksum(sha1HashPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        component.setHashes(List.of(new Hash("sha1", sha1Checksum)));

        component.setBomRef(component.getPurl());

        return component;
    }

    public Collection<Path> libraryJars() {
        return libraryJars;
    }
}
