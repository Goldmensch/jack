package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RunTask extends Task {

    public static final Logger log = LoggerFactory.getLogger(RunTask.class);

    private final Jack jack;

    public RunTask(Jack jack) {
        super(jack, TaskType.BUILD);
        this.jack = jack;
    }

    @Override
    public void run() throws IOException, InterruptedException {
        BuildTask buildTask = dependency(TaskType.BUILD);
        Path jarPath = buildTask.jarPath();
        String libClassPath = buildTask.libClassPath();

        var classPath = jarPath + ":" + libClassPath;

        var finalArgs = new ArrayList<>(List.of("java", "-cp", classPath, jack.projectConfig().manifest().mainClass()));

        log.info("Running with arguments {}", jack.providedArgs());

        finalArgs.addAll(jack.providedArgs());
        var process = new ProcessBuilder(finalArgs)
                .inheritIO()
                .start();
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
