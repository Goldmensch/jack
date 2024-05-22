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
    public Void run() throws IOException, InterruptedException {
        BuildTask buildTask = new BuildTask(jack);
        Path jarPath = buildTask.run();
        String libClassPath = buildTask.libClassPath();

        var classPath = jarPath + ":" + libClassPath;

        var finalArgs = new ArrayList<>(List.of("java", "-cp", classPath, jack.config().manifest().mainClass()));
        finalArgs.addAll(Arrays.asList(args));
        var process = new ProcessBuilder(finalArgs)
                .inheritIO()
                .start();
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            System.exit(exitCode);
        }

        return null;
    }
}
