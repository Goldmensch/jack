package io.github.goldmensch.config;

public record SemVer(
        int major,
        int minor,
        int patch
) {
    public static SemVer of(String version) {
        String[] parts = version.split("\\.");
        return switch (parts.length) {
            case 2 -> new SemVer(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), -1);
            case 3 -> new SemVer(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            default -> throw new IllegalArgumentException("Invalid SemVer version: " + version);
        };
    }

    public String versionString() {
        return "%s.%s.%s".formatted(major, minor, patch);
    }
}
