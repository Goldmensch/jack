package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import io.github.goldmensch.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class BuildTask extends Task {

    private Path jarPath;

    public BuildTask(Jack jack) {
        super(jack, TaskType.DEPENDENCIES);
    }

    @Override
    public void run() {
        try {
            FileUtils.deleteRecursively(jack.paths().classes());
            FileUtils.deleteRecursively(jack.paths().jars());

            compileClasses();
            createJar();

            createDistribution();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDistribution() throws IOException {
        FileUtils.deleteRecursively(jack.paths().distributions());
        Files.createDirectories(jack.paths().distributions());
        Files.createDirectories(jack.paths().distributionLibs());

        for (Path libPath : ((DependenciesTask) dependency(TaskType.DEPENDENCIES)).libraryJars()) {
            Files.copy(libPath, jack.paths().distributionLibs().resolve(libPath.getFileName()));
        }

        Files.copy(jarPath, jack.paths().distributions().resolve(jarPath.getFileName()));
    }

    private void compileClasses() throws IOException, InterruptedException {
        Files.createDirectories(jack.paths().classes());

        var args = new ArrayList<>(List.of("javac", "-d", jack.paths().classes().toString(), "-cp", libClassPath()));
        jack.sourceSet().files()
                .stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .forEach(args::add);

        var process = new ProcessBuilder(args)
                .inheritIO()
                .start();
        process.waitFor();
    }

    private void createJar() throws IOException, InterruptedException {
        this.jarPath = jack.paths().out().resolve(Path.of("jars", jack.projectConfig().project().name()) + ".jar");
        var jarArgs = new ArrayList<String>();

        jarArgs.addAll(List.of("jar", "--create", "--file", jarPath.toString(), "--main-class", jack.projectConfig().manifest().mainClass(), "-C", jack.paths().classes().toString(), "."));
        if (Files.exists(jack.paths().resources())) {
            jarArgs.addAll(List.of("-C", jack.paths().resources().toString(), "."));
        }

        new ProcessBuilder(jarArgs)
                .inheritIO()
                .start()
                .waitFor();
    }

    public String libClassPath() {
        return ((DependenciesTask) dependency(TaskType.DEPENDENCIES))
                .libraryJars()
                .stream()
                .map(path -> path.toAbsolutePath().toString())
                .collect(Collectors.joining(":"));
    }

    public Path jarPath() {
        return jarPath;
    }
}
