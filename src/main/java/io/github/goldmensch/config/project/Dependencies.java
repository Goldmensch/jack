package io.github.goldmensch.config.project;

import io.github.goldmensch.config.project.catalog.Catalog;
import org.tomlj.TomlTable;

import java.util.Collection;
import java.util.List;

public record Dependencies(
        Collection<Dependency> dependencies
) {
    static Dependencies of(Values values, Catalog catalog) {
        List<Dependency> dependencies = values.entrySet()
                .stream()
                .flatMap(entry -> {
                    var groupId = entry.getKey();
                    return  ((TomlTable) entry.getValue()).entrySet()
                            .stream()
                            .map(subStreamEntry -> Dependency.of(groupId, subStreamEntry, catalog));
                })
                .toList();
        return new Dependencies(dependencies);
    }
}
