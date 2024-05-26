package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;

public enum TaskType {
    BUILD,
    RUN,
    DEPENDENCIES
    ;

    public Task instantiate(Jack jack) throws NoLocalRepositoryManagerException {
        return switch (this) {
            case BUILD -> new BuildTask(jack);
            case RUN -> new RunTask(jack);
            case DEPENDENCIES -> new DependenciesTask(jack);
        };
    }

    public static TaskType of(Task task) {
        return switch (task) {
            case BuildTask ignored -> BUILD;
            case RunTask ignored -> RUN;
            case DependenciesTask ignored -> DEPENDENCIES;
        };
    }
}
