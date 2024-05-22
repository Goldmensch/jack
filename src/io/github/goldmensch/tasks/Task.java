package io.github.goldmensch.tasks;

import java.io.IOException;

public sealed interface Task<T> permits BuildTask, RunTask {
    T run() throws IOException, InterruptedException;
}
