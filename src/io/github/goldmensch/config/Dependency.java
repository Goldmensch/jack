package io.github.goldmensch.config;

import java.util.Map;

public record Dependency(
        String groupId,
        String artifactId,
        SemVer version
) {

    static Dependency of(Map.Entry<String, Object> entry) {
        var version = SemVer.of(((String) entry.getValue()));
        return of(entry.getKey(), version);
    }

    public static Dependency of(String id, SemVer version) {
        String[] parts = id.split(":");
        return new Dependency(parts[0], parts[1], version);
    }

    public String[] groupIdParts() {
        return groupId.split("\\.");
    }
}
