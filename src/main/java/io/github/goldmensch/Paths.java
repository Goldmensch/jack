package io.github.goldmensch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Path outResources,
        Path sbom
) {
    private static final Logger log = LoggerFactory.getLogger(Paths.class);

    static Paths create() {
        try {
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

            String jackConfigLocation = System.getenv("JACK_CONFIG_LOCATION");
            var config = jackConfigLocation == null
                    ? root.resolve("jack.toml")
                    : root.resolve(Path.of(jackConfigLocation));
            var source = root.resolve("src");
            var resources = root.resolve("resources");
            var sbom = root.resolve("bom.json");
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
                    outResources,
                    sbom
            );
        } catch (IOException e) {
            log.error("Error while trying to create directories and files", e);
            throw new AbortProgramException();
        }
    }
}
