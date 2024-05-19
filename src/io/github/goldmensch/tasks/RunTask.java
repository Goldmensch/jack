package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RunTask implements Task<Void> {

    private final Jack jack;
    private final String[] args;

    public RunTask(Jack jack, String[] args) {
        this.jack = jack;
        this.args = args;
    }

    @Override
    public Void run() throws IOException {
        BuildTask buildTask = new BuildTask(jack);
        Path jarPath = buildTask.run();
        String libClassPath = buildTask.libClassPath();

        var classPath = jarPath + ":" + libClassPath;

        var finalArgs = new ArrayList<>(List.of("java", "-cp", classPath, jack.config().mainClass()));
        finalArgs.addAll(Arrays.asList(args));
        new ProcessBuilder(finalArgs)
                .inheritIO()
                .start();

        return null;
    }
}
