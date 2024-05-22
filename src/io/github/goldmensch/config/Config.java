package io.github.goldmensch.config;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.github.goldmensch.config.Values.required;

public record Config(
        Project project,
        Manifest manifest,
        Dependencies dependencies,
        Packaging packaging,
        Repositories repositories
) {
    public static Config read(Path root) throws IOException {
        var source = root.resolve("jack.toml");
        TomlParseResult parseResult = Toml.parse(source);

        if (parseResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid toml file: " + source + "\n" + parseResult.errors());
        }

        return createConfig(new Values(parseResult));
    }

    private static Config createConfig(Values values) {
        return new Config(
                required(values.parseCategory("project", Project::of)),
                values.parseCategory("manifest", Manifest::of),
                values.parseCategory("dependencies", Dependencies::of),
                values.parseCategory("packaging", Packaging::of),
                values.parseCategory("repositories", Repositories::of)
        );
    }
}
