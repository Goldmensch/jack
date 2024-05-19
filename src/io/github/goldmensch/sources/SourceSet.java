package io.github.goldmensch.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceSet {
    private final Set<Path> files;

    private SourceSet(Set<Path> files) {
        this.files = files;
    }

    public static SourceSet read(Path root) throws IOException {
        Set<Path> files = Files.walk(root.resolve("src"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toUnmodifiableSet());
        return new SourceSet(files);
    }

    public Set<Path> files() {
        return files;
    }
}
