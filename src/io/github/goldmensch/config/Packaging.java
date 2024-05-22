package io.github.goldmensch.config;

public record Packaging(
        Type type
) {
    static Packaging of(Values values) {
        var type = values.parse(values.string("type"), Type::of);
        return new Packaging(type);
    }

    public enum Type {
        JAR
        ;

        static Type of(String type) {
            return switch (type) {
                case "jar" -> JAR;
                default -> throw new IllegalArgumentException("Unknown packaging type: %s".formatted(type));
            };
        }
    }
}
