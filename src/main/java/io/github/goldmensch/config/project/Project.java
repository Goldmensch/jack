package io.github.goldmensch.config.project;

import java.util.List;

import static io.github.goldmensch.config.project.Values.required;

public record Project(
        String name,
        SemVer version,
        List<String> authors
) {
    static Project of(Values values) {
        var name = required(values.string("name"));
        var version = required(values.semVer("version"));
        var authors = readAuthors(values);
        return new Project(name, version, authors);
    }

    private static List<String> readAuthors(Values values) {
        String author = values.string("author");
        if (author != null) return List.of(author);
        return required(values.list("authors"))
                .stream()
                .map(String.class::cast)
                .toList();
    }
}
