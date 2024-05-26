package io.github.goldmensch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Paths(
        Path root,
        Path jack,
        Path caches,
        Path config,
        Path source,
        Path out,
        Path classes,
        Path jars,
        Path mavenCache,
        Path distributions,
        Path distributionLibs,
        Path resources,
        Path outResources
) {
    static Paths create() throws IOException {
        var root = Files.createDirectories(Path.of(""));
        var jack = Files.createDirectories(root.resolve(".jack"));
        var caches = Files.createDirectories(jack.resolve("caches"));
        var out = Files.createDirectories(root.resolve("out"));
        var classes = Files.createDirectories(out.resolve("classes"));
        var jars = Files.createDirectories(out.resolve("jars"));
        var mavenCache = Files.createDirectories(caches.resolve("maven"));
        var distributions = Files.createDirectories(out.resolve("distributions"));
        var distributionLibs = Files.createDirectories(distributions.resolve("libs"));
        var outResources = Files.createDirectories(out.resolve("resources"));


        var config = root.resolve("jack.toml");
        var source = root.resolve("src");
        var resources = root.resolve("resources");
        return new Paths(
                root,
                jack,
                caches,
                config,
                source,
                out,
                classes,
                jars,
                mavenCache,
                distributions,
                distributionLibs,
                resources,
                outResources
        );
    }
}
