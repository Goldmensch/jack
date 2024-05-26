package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
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
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public final class DependenciesTask extends Task {

    public static final Logger log = LoggerFactory.getLogger(DependenciesTask.class);
    
    private static final RepositorySystemSupplier REPOSITORY_SYSTEM_SUPPLIER = new RepositorySystemSupplier();

    private final RepositorySystemSession session;
    private final RepositorySystem repositorySystem;
    private final List<RemoteRepository> resolutionRepositories;

    private Collection<Path> libraryJars;

    public DependenciesTask(Jack jack) {
        super(jack);
        this.repositorySystem = REPOSITORY_SYSTEM_SUPPLIER.get();
        var localRepository = new LocalRepository(jack.paths().mavenCache().toFile());

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
                .setSystemProperty("java.version", "21");

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
    protected void run() throws IOException, InterruptedException, ArtifactResolutionException, NoLocalRepositoryManagerException, DependencyCollectionException, DependencyResolutionException, ArtifactDescriptorException {
        List<org.eclipse.aether.graph.Dependency> dependencies = jack.projectConfig().dependencies().dependencies()
                .stream()
                .map(dependency -> {
                    DefaultArtifact defaultArtifact = new DefaultArtifact(dependency.groupId(), dependency.artifactId(), "jar", dependency.version());
                    return new org.eclipse.aether.graph.Dependency(defaultArtifact, JavaScopes.RUNTIME);
                })
                .toList();


        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRepositories(resolutionRepositories);
        collectRequest.setDependencies(dependencies);

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

        DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);

        result.getArtifactResults()
                .forEach(resultEntry -> log.info("Resolved dependency: {}", resultEntry));
        
        if (jack.providedArgs().contains("graph")) {
            DependencyGraphDumper dependencyGraphDumper = new DependencyGraphDumper(System.out::println);
            result.getRoot().accept(dependencyGraphDumper);
        }

        libraryJars = result.getArtifactResults()
                .stream()
                .map(artifactResult -> artifactResult.getArtifact().getFile().toPath())
                .toList();
    }

    public Collection<Path> libraryJars() {
        return libraryJars;
    }
}
