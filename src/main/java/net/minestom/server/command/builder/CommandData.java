package net.minestom.server.command.builder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandData {

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    @Contract("_, _ -> this")
    public @NotNull CommandData set(@NotNull String key, @Nullable Object value) {
        this.dataMap.put(key, value);
        return this;
    }

    public @Nullable <T> T get(@NotNull String key) {
        //noinspection unchecked
        return (T) dataMap.get(key);
    }

    public boolean has(@NotNull String key) {
        return dataMap.containsKey(key);
    }

    public void clear() {
        this.dataMap.clear();
    }

    public @NotNull Map<String, Object> getDataMap() {
        return dataMap;
    }
}
