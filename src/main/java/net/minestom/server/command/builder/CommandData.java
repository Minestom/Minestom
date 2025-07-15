package net.minestom.server.command.builder;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandData {

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public CommandData set(String key, Object value) {
        this.dataMap.put(key, value);
        return this;
    }

    @Nullable
    public <T> T get(String key) {
        return (T) dataMap.get(key);
    }

    public boolean has(String key) {
        return dataMap.containsKey(key);
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}
