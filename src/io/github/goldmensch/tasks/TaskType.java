package io.github.goldmensch.tasks;

public enum TaskType {
    BUILD,
    RUN
    ;

    public static TaskType of(Task task) {
        return switch (task) {
            case BuildTask ignored -> BUILD;
            case RunTask ignored -> RUN;
        };
    }
}
