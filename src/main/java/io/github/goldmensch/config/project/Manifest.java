package io.github.goldmensch.config.project;

public record Manifest(
        String mainClass
) {
    static Manifest of(Values values) {
        return new Manifest(values.string("main"));
    }
}
