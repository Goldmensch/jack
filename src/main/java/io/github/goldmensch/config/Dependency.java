package io.github.goldmensch.config;

public record Dependency(
        String groupId,
        String artifactId,
        SemVer version
) {
    public static Dependency of(String id, SemVer version) {
        String[] parts = id.split(":");
        return new Dependency(parts[0], parts[1], version);
    }

    public String[] groupIdParts() {
        return groupId.split("\\.");
    }
}
