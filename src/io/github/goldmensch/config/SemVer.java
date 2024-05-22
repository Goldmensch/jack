package io.github.goldmensch.config;

public record SemVer(
        int major,
        int minor,
        int patch,
        String preRelease,
        String metadata
) {
    public static SemVer of(String version) {
        String preRelease = null;
        String metadata = null;
        String[] metadataParts = version.split("\\+", 2);
        if (metadataParts.length > 1) {
            metadata = metadataParts[1];
        }
        String[] preReleaseParts = metadataParts[0].split("-", 2);
        if (preReleaseParts.length > 1) {
            preRelease = preReleaseParts[1];
        }
        String[] versionParts = preReleaseParts[0].split("\\.");
        return switch (versionParts.length) {
            case 1 -> new SemVer(Integer.parseInt(versionParts[0]), -1, -1, preRelease, metadata);
            case 2 -> new SemVer(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]), -1, preRelease, metadata);
            case 3 -> new SemVer(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]), Integer.parseInt(versionParts[2]), preRelease, metadata);
            default -> throw new IllegalArgumentException("Invalid SemVer version: " + version);
        };
    }

    public String versionString() {
        String string = major + "";
        if (minor != -1) string += "." + minor;
        if (patch != -1) string += "." + patch;
        if (preRelease != null) string += "-" + preRelease;
        if (metadata != null) string += "+" + metadata;
        return string;
    }
}
