package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;

import java.io.IOException;
import java.util.*;

public sealed abstract class Task permits BuildTask, RunTask {

    private final Jack jack;
    private final Collection<TaskType> dependencyTypes;
    private Map<TaskType, Task> dependencies;

    public Task(Jack jack, TaskType... dependencyTypes) {
        this.jack = jack;
        this.dependencyTypes = Arrays.asList(dependencyTypes);

    }

    public void process() throws IOException, InterruptedException {
        processWithDependencies(new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    protected <T extends Task> T dependency(TaskType type) {
        if (!dependencies.containsKey(type)) throw new IllegalArgumentException("Task %s isn't a dependency of this task".formatted(type));
        return (T) dependencies.get(type);
    }

    protected abstract void run() throws IOException, InterruptedException;

    private void processWithDependencies(Map<TaskType, Task> ranTasks) throws IOException, InterruptedException {
        for (TaskType type : dependencyTypes) {
            if (ranTasks.containsKey(type)) continue;
            Task task = instantiate(type);
            task.processWithDependencies(ranTasks);
            ranTasks.put(type, task);
        }
        this.dependencies = ranTasks;
        run();
    }


    private Task instantiate(TaskType type) {
        return switch (type) {
            case BUILD -> new BuildTask(jack);
            case RUN -> new RunTask(jack);
        };
    }
}
