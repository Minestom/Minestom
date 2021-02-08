package net.minestom.server.command.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandData {

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public void set(@NotNull String key, Object value) {
        this.dataMap.put(key, value);
    }

    @Nullable
    public <T> T get(@NotNull String key) {
        return (T) dataMap.get(key);
    }

    @NotNull
    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}
