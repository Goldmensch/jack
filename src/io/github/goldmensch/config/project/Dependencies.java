package io.github.goldmensch.config.project;

import java.util.Collection;
import java.util.List;

public record Dependencies(
        Collection<Dependency> dependencies
) {
    static Dependencies of(Values values) {
        List<Dependency> dependencies = values.entrySet()
                .stream()
                .map(Dependency::of)
                .toList();
        return new Dependencies(dependencies);
    }
}
