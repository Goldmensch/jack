package io.github.goldmensch.config.project;

import java.util.Map;

public record Dependency(
        String groupId,
        String artifactId,
        String version
) {

    static Dependency of(Map.Entry<String, Object> entry) {
        return of(entry.getKey(), ((String) entry.getValue()));
    }

    public static Dependency of(String id, String version) {
        String[] parts = id.split(":");
        return new Dependency(parts[0], parts[1], version);
    }
}
