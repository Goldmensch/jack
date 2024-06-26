package io.github.goldmensch.config.project;

import java.util.Collection;
import java.util.List;

public record Repositories(
        Collection<Repository> repositories
) {
    static Repositories of(Values values) {
        List<Repository> repositories = values.entrySet()
                .stream()
                .map(Repository::of)
                .toList();
        return new Repositories(repositories);
    }
}
