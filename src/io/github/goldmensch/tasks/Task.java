package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.io.IOException;
import java.util.*;

public sealed abstract class Task permits BuildTask, DependenciesTask, RunTask {

    protected final Jack jack;
    private final Collection<TaskType> dependencyTypes;
    private Map<TaskType, Task> dependencies;

    public Task(Jack jack, TaskType... dependencyTypes) {
        this.jack = jack;
        this.dependencyTypes = Arrays.asList(dependencyTypes);
    }

    public void process() throws IOException, InterruptedException, ArtifactResolutionException, NoLocalRepositoryManagerException, DependencyCollectionException, DependencyResolutionException, ArtifactDescriptorException {
        processWithDependencies(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    protected <T extends Task> T dependency(TaskType type) {
        if (!dependencies.containsKey(type)) throw new IllegalArgumentException("Task %s isn't a dependency of this task".formatted(type));
        return (T) dependencies.get(type);
    }

    protected abstract void run() throws IOException, InterruptedException, ArtifactResolutionException, NoLocalRepositoryManagerException, DependencyCollectionException, DependencyResolutionException, ArtifactDescriptorException;

    private void processWithDependencies(Map<TaskType, Task> ranTasks) throws IOException, InterruptedException, ArtifactResolutionException, NoLocalRepositoryManagerException, DependencyCollectionException, DependencyResolutionException, ArtifactDescriptorException {
        for (TaskType type : dependencyTypes) {
            if (ranTasks.containsKey(type)) continue;
            Task task = type.instantiate(jack);
            task.processWithDependencies(ranTasks);
            ranTasks.put(type, task);
        }
        this.dependencies = ranTasks;
        run();
    }
}
