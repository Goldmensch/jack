package io.github.goldmensch.config;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public record Config(
        String name,
        SemVer version,
        String mainClass,
        List<Dependency> dependencies
) {
    public static Config read(Path root) throws IOException {
        var source = root.resolve("jack.toml");
        TomlParseResult parseResult = Toml.parse(source);

        if (parseResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid toml file: " + source + "\n" + parseResult.errors());
        }

        return createConfig(parseResult);
    }

    private static Config createConfig(TomlTable result) {
        var name = result.getString("name");
        var version = SemVer.of(result.getString("version"));
        var mainClass = result.getString("main");
        TomlTable dependencyTable = result.getTable("dependencies");


        return new Config(name, version, mainClass, parseDependencies(dependencyTable));
    }

    private static List<Dependency> parseDependencies(TomlTable depTable) {
        return depTable.entrySet()
                .stream()
                .map(entry -> {
                    var composedId = entry.getKey();
                    var version = SemVer.of(((String) entry.getValue()));
                    return Dependency.of(composedId, version);
                })
                .toList();
    }
}
