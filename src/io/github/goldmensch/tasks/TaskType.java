package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;

public enum TaskType {
    BUILD,
    RUN,
    DEPENDENCIES,
    BOM
    ;

    public Task instantiate(Jack jack) throws NoLocalRepositoryManagerException {
        return switch (this) {
            case BUILD -> new BuildTask(jack);
            case RUN -> new RunTask(jack);
            case DEPENDENCIES -> new DependenciesTask(jack);
            case BOM -> new CreateBOMTask(jack);
        };
    }

    public static TaskType of(Task task) {
        return switch (task) {
            case BuildTask ignored -> BUILD;
            case RunTask ignored -> RUN;
            case DependenciesTask ignored -> DEPENDENCIES;
            case CreateBOMTask ignored -> BOM;
        };
    }
}
