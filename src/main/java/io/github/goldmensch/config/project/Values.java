package io.github.goldmensch.config.project;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Values {
    private final TomlTable parent;
    private final TomlTable root;

    Values(TomlTable parent, TomlTable root) {
        this.parent = parent;
        this.root = root;
    }

    public <T> T parseCategory(String category, Function<Values, T> parser) {
        TomlTable table = root.getTable(category);
        return table != null
                ? parser.apply(new Values(parent, table))
                : null;
    }

    public <T, R> R parse(T arg, Function<T, R> parser) {
        return arg != null
                ? parser.apply(arg)
                : null;
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return root.entrySet();
    }

    public Set<Map.Entry<String, Object>> dottedEntrySet(boolean includeTables) {
        return root.dottedEntrySet(includeTables);
    }

    public String string(String key) {
        return root.getString(key);
    }

    public SemVer semVer(String key) {
        String string = string(key);
        return string != null
                ? SemVer.of(string)
                : null;
    }

    public List<Object> list(String key) {
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
