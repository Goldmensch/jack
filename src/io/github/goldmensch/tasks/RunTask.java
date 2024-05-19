package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;

import java.io.IOException;
import java.nio.file.Path;

public final class RunTask implements Task<Void> {

    private final Jack jack;

    public RunTask(Jack jack) {
        this.jack = jack;
    }

    @Override
    public Void run() throws IOException {
        BuildTask buildTask = new BuildTask(jack);
        Path jarPath = buildTask.run();
        String libClassPath = buildTask.libClassPath();

        var classPath = jarPath + ":" + libClassPath;
        new ProcessBuilder("java", "-cp", classPath, jack.config().mainClass())
                .inheritIO()
                .start();
        return null;
    }
}
