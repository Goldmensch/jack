package io.github.goldmensch.config;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class Values {
    private final TomlTable root;

    Values(TomlTable root) {
        this.root = root;
    }

    <T> T parseCategory(String category, Function<Values, T> parser) {
        TomlTable table = root.getTable(category);
        return table != null
                ? parser.apply(new Values(table))
                : null;
    }

    <T, R> R parse(T arg, Function<T, R> parser) {
        return arg != null
                ? parser.apply(arg)
                : null;
    }

    Set<Map.Entry<String, Object>> entrySet() {
        return root.entrySet();
    }

    String string(String key) {
        return root.getString(key);
    }

    SemVer semVer(String key) {
        String string = string(key);
        return string != null
                ? SemVer.of(string)
                : null;
    }

    List<Object> list(String key) {
        TomlArray array = root.getArray(key);
        return array != null
                ? array.toList()
                : null;
    }

    static <T> T required(T val) {
        if (val != null) return val;
        throw new IllegalArgumentException("This field is required!");
    }
}
