package net.minestom.server.command.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandData {

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public CommandData set(@NotNull String key, Object value) {
        this.dataMap.put(key, value);
        return this;
    }

    @Nullable
    public <T> T get(@NotNull String key) {
        return (T) dataMap.get(key);
    }

    public boolean has(@NotNull String key) {
        return dataMap.containsKey(key);
    }

    @NotNull
    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}
