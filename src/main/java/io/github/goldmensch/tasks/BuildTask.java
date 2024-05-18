package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import io.github.goldmensch.config.Config;
import io.github.goldmensch.config.Dependency;
import io.github.goldmensch.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class BuildTask implements Task<Path> {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Jack jack;
    private final Path outDir;
    private final Path classDir;
    private final Path librariesDir;

    private final List<Path> libPaths = new ArrayList<>();

    public BuildTask(Jack jack) {
        this.jack = jack;
        this.outDir = jack.root().resolve(Path.of("out")).toAbsolutePath();
        this.classDir = outDir.resolve("classes");
        this.librariesDir = outDir.resolve("libs");
    }

    @Override
    public Path run() {
        try {
            libPaths.clear();
            FileUtils.deleteRecursively(jack.root().resolve("out"));

            downloadLibraries();
            compileClasses();
            return createJar();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadLibraries() throws IOException, InterruptedException {
        FileUtils.deleteRecursively(librariesDir);
        Files.createDirectories(librariesDir);
        List<Dependency> dependencies = jack.config().dependencies();

        for (Dependency dependency : dependencies) {
            System.out.printf("Downloading library: %s%n", dependency);

            var resource = dependency.artifactId() + "-" + dependency.version().versionString() + ".jar";
            var request = buildMavenRequest(dependency, "https://repo.maven.apache.org/maven2/", resource);
            Path path = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(librariesDir.resolve(resource))).body();
            libPaths.add(path);
        }
    }

    private static HttpRequest buildMavenRequest(Dependency dependency, String root, String resource) {
        String group = dependency.groupId().replace(".", "/");
        var url =  group + "/" + dependency.artifactId() + "/" + dependency.version().versionString() + "/" + resource;
        return HttpRequest.newBuilder(URI.create(root + url)).build();
    }

    private void compileClasses() throws IOException, InterruptedException {
        Files.createDirectories(classDir);

        var args = new ArrayList<>(List.of("javac", "-d", classDir.toString(), "-cp", libClassPath()));
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

    private Path createJar() throws IOException, InterruptedException {
        var outPath = outDir.resolve(Path.of("jars", jack.config().name() + ".jar"));
        var jarArgs = List.of("jar", "--create", "--file", outPath.toString(), "--main-class", jack.config().mainClass(), "-C", classDir.toString(), ".");

        new ProcessBuilder(jarArgs)
                .inheritIO()
                .start()
                .waitFor();
        return outPath;
    }

    public String libClassPath() {
        return libPaths
                .stream()
                .map(path -> path.toAbsolutePath().toString())
                .collect(Collectors.joining(":"));
    }
}
