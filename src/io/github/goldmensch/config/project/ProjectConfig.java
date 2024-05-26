package io.github.goldmensch.config.project;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Path;

import static io.github.goldmensch.config.project.Values.required;

public record ProjectConfig(
        Project project,
        Manifest manifest,
        Dependencies dependencies,
        Packaging packaging,
        Repositories repositories
) {
    public static ProjectConfig read(Path config) throws IOException {
        TomlParseResult parseResult = Toml.parse(config);

        if (parseResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid toml file: " + config + "\n" + parseResult.errors());
        }

        return createConfig(new Values(parseResult));
    }

    private static ProjectConfig createConfig(Values values) {
        return new ProjectConfig(
                required(values.parseCategory("project", Project::of)),
                values.parseCategory("manifest", Manifest::of),
                values.parseCategory("dependencies", Dependencies::of),
                values.parseCategory("packaging", Packaging::of),
                values.parseCategory("repositories", Repositories::of)
        );
    }
}
