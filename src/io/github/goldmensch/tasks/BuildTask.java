package io.github.goldmensch.tasks;

import io.github.goldmensch.Jack;
import io.github.goldmensch.config.Dependency;
import io.github.goldmensch.config.SemVer;
import io.github.goldmensch.utils.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class BuildTask extends Task {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Jack jack;
    private final Path outDir;
    private final Path classDir;
    private final Path librariesDir;

    private Path jarPath;

    private final List<Path> libPaths = new ArrayList<>();

    public BuildTask(Jack jack) {
        super(jack);
        this.jack = jack;
        this.outDir = jack.root().resolve(Path.of("out")).toAbsolutePath();
        this.classDir = outDir.resolve("classes");
        this.librariesDir = outDir.resolve("libs");
    }

    @Override
    public void run() {
        try {
            libPaths.clear();
            FileUtils.deleteRecursively(jack.root().resolve("out"));

            downloadLibraries();
            compileClasses();
            createJar();
        } catch (IOException | InterruptedException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadLibraries() throws IOException, InterruptedException, XmlPullParserException {
        FileUtils.deleteRecursively(librariesDir);
        Files.createDirectories(librariesDir);
        for (Dependency dependency : jack.config().dependencies().dependencies()) {
            downloadDependency(dependency);
        }
    }

    private static HttpRequest buildMavenRequest(Dependency dependency, String root, String resource) {
        String group = dependency.groupId().replace(".", "/");
        var url =  group + "/" + dependency.artifactId() + "/" + dependency.version().versionString() + "/" + resource;
        return HttpRequest.newBuilder(URI.create(root + url)).build();
    }

    private void downloadDependency(Dependency dependency) throws IOException, InterruptedException, XmlPullParserException {
        System.out.printf("Downloading library: %s%n", dependency);

        var resourcePrefix = dependency.artifactId() + "-" + dependency.version().versionString();
        Model pom = fetchPom(dependency);
        Model parent = pom.getParent() != null
                ? fetchParent(pom.getParent())
                : null;
        for (org.apache.maven.model.Dependency pomModelDependency : pom.getDependencies()) {
            String scope = pomModelDependency.getScope();
            if (scope != null && !"runtime".equals(scope)) continue;

            String artifactId = pomModelDependency.getArtifactId();
            String groupId = pomModelDependency.getGroupId();
            String version = pomModelDependency.getVersion();

            if (version == null) {
                System.out.println("must be in parent");
                System.out.println(artifactId);
                System.out.println(groupId);
                System.out.println(parent.getDependencies());
                version = parent.getDependencyManagement().getDependencies()
                        .stream()
                        .filter(item -> artifactId.equals(item.getArtifactId()) && groupId.equals(item.getGroupId()))
                        .findAny()
                        .orElseThrow()
                        .getVersion();

                if (version.startsWith("$")) {
                    if (version.equals("${project.version}")) {
                        version = parent.getVersion();
                    } else {
                        version = parent.getProperties().getProperty(version.replaceAll("[${}]", ""));
                    }
                }
            }

            System.out.println("DOOP: " + pomModelDependency);
            System.out.println(version);
            var jackDep = new Dependency(groupId, artifactId, SemVer.of(version));
            downloadDependency(jackDep);
        }

        var jarResource = resourcePrefix + ".jar";
        HttpRequest jarRequest = buildMavenRequest(dependency, "https://repo.maven.apache.org/maven2/", jarResource);
        Path path = httpClient.send(jarRequest, HttpResponse.BodyHandlers.ofFile(librariesDir.resolve(jarResource))).body();
        libPaths.add(path);
    }

    private Model fetchParent(Parent parent) throws XmlPullParserException, IOException, InterruptedException {
        String artifactId = parent.getArtifactId();
        String groupId = parent.getGroupId();
        String version = parent.getVersion();
        return fetchPom(new Dependency(groupId, artifactId, SemVer.of(version)));
    }

    private Model fetchPom(Dependency dependency) throws IOException, InterruptedException, XmlPullParserException {
        System.out.println("fetching pom" + dependency);

        var resourcePrefix = dependency.artifactId() + "-" + dependency.version().versionString();

        HttpRequest pomRequest = buildMavenRequest(dependency, "https://repo.maven.apache.org/maven2/", resourcePrefix + ".pom");
        InputStream pom = httpClient.send(pomRequest, HttpResponse.BodyHandlers.ofInputStream()).body();
        return new MavenXpp3Reader().read(pom);
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

    private void createJar() throws IOException, InterruptedException {
        this.jarPath = outDir.resolve(Path.of("jars", jack.config().project().name()) + ".jar");
        var jarArgs = List.of("jar", "--create", "--file", jarPath.toString(), "--main-class", jack.config().manifest().mainClass(), "-C", classDir.toString(), ".");

        new ProcessBuilder(jarArgs)
                .inheritIO()
                .start()
                .waitFor();
    }

    public String libClassPath() {
        return libPaths
                .stream()
                .map(path -> path.toAbsolutePath().toString())
                .collect(Collectors.joining(":"));
    }

    public Path jarPath() {
        return jarPath;
    }
}
