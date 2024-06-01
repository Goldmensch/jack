package io.github.goldmensch.config.project;

import io.github.goldmensch.AbortProgramException;
import io.github.goldmensch.config.project.catalog.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Path;

public record ProjectConfig(
        Project project,
        Manifest manifest,
        Catalog catalog,
        Dependencies dependencies,
        Packaging packaging,
        Repositories repositories
) {
    private static final Logger log = LoggerFactory.getLogger(ProjectConfig.class);

    public static ProjectConfig read(Path config) {
        TomlParseResult parseResult = null;
        try {
            parseResult = Toml.parse(config);
        } catch (IOException e) {
            log.error("Error while trying to read toml file at {}", config, e);
            throw new AbortProgramException();
        }

        if (parseResult.hasErrors()) {
            log.error("Invalid toml file at {} with errors {}", config, parseResult.errors());
            throw new AbortProgramException();
        }

        return createConfig(new Values(parseResult, parseResult));
    }

    private static ProjectConfig createConfig(Values values) {
        var catalog = values.parseCategory("catalog", Catalog::of);

        return new ProjectConfig(
                Values.required(values.parseCategory("project", Project::of)),
                values.parseCategory("manifest", Manifest::of),
                catalog,
                values.parseCategory("dependencies", subValues -> Dependencies.of(subValues, catalog)),
                values.parseCategory("packaging", Packaging::of),
                values.parseCategory("repositories", Repositories::of)
        );
    }
}
