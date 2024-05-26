package io.github.goldmensch.config.project.catalog;

import io.github.goldmensch.config.project.Values;

import java.util.Map;
import java.util.stream.Collectors;

public record Catalog(
        Map<String, CatalogEntry> entries
) {
    public static Catalog of(Values values) {
        Map<String, CatalogEntry> entries = values.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), CatalogEntry.of(entry)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Catalog(entries);
    }
}
