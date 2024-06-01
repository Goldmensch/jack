package io.github.goldmensch.sources;

import io.github.goldmensch.AbortProgramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceSet {
    private static final Logger log = LoggerFactory.getLogger(SourceSet.class);
    private final Set<Path> files;

    private SourceSet(Set<Path> files) {
        this.files = files;
    }

    public static SourceSet read(Path source) {
        try(var paths = Files.walk(source)) {
            var validFiles = paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toUnmodifiableSet());
            return new SourceSet(validFiles);
        } catch (IOException e) {
            log.error("Error while walking source file path, startint at {}", source, e);
            throw new AbortProgramException();
        }
    }

    public Set<Path> files() {
        return files;
    }
}
