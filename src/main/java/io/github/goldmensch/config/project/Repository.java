package io.github.goldmensch.config.project;

import java.util.Map;

public record Repository(
        String url,
        Type type
) {
    static Repository of(Map.Entry<String, Object> entry) {

        var defaultFound = switch (entry.getKey()) {
            case "mavenCentral" -> new Repository("https://repo.maven.apache.org/maven2/", Type.MAVEN);
            default -> null;
        };

        if (defaultFound != null) {
            return entry.getValue().equals(true)
                    ? defaultFound
                    : null;
        }

        var url = entry.getKey();
        var type = Type.of(((String) entry.getValue()));
        return new Repository(url, type);
    }

    public enum Type {
        MAVEN
        ;
        static Type of(String type) {
            return switch (type) {
                case "maven" -> MAVEN;
                default -> throw new IllegalArgumentException("Unknown repository type: %s".formatted(type));
            };
        }
    }
}
