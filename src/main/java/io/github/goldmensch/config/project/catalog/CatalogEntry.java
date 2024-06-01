package io.github.goldmensch.config.project.catalog;

import org.tomlj.TomlTable;

import java.util.Map;

public record CatalogEntry(
        String version,
        String forGroup
) {
    public static CatalogEntry of(Map.Entry<String, Object> entry) {
        String version;
        String forGroup = null;

        switch (entry.getValue()) {
            case String readVersion -> version = readVersion;
            case TomlTable table -> {
                forGroup = table.getString("for.group");
                version = table.getString("version");
            }
            default -> throw new IllegalArgumentException("catalog entry type cannot be something else! Found: %s".formatted(entry.getValue()));
        }
        return new CatalogEntry(version, forGroup);
    }

    public boolean isForGroup(String group) {
        return forGroup != null && forGroup.equals(group);
    }
}
