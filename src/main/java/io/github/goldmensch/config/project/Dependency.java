package io.github.goldmensch.config.project;

import io.github.goldmensch.config.project.catalog.Catalog;
import org.tomlj.TomlTable;

import java.util.Map;

public record Dependency(
        String groupId,
        String artifactId,
        String version
) {

    static Dependency of(String groupId, Map.Entry<String, Object> entry, Catalog catalog) {
        var version = switch (entry.getValue()) {
            case String directVersion -> directVersion;
            case TomlTable table -> resolveVersion(table, catalog, groupId);
            default -> throw new IllegalArgumentException("Version or table expected found %s".formatted(entry.getValue()));
        };

        return new Dependency(
                groupId,
                entry.getKey(),
                version
        );
    }

    private static String resolveVersion(TomlTable table, Catalog catalog, String groupId) {
        if (table.isEmpty()) {
            return catalog.entries()
                    .values()
                    .stream()
                    .filter(entry -> entry.isForGroup(groupId))
                    .findFirst()
                    .orElseThrow()
                    .version();
        }

        String versionRef = table.getString("version.ref");
        if (versionRef != null) return catalog.entries().get(versionRef).version();
        throw new IllegalStateException("Cannot find version for dependency with groupid %s".formatted(groupId));
    }

}
