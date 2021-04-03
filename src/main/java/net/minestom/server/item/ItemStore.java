package net.minestom.server.item;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemStore {

    private final Map<String, ItemStoreBuilder.Entry<?>> entryMap;

    protected ItemStore(@NotNull Map<String, ItemStoreBuilder.Entry<?>> entryMap) {
        this.entryMap = Collections.unmodifiableMap(entryMap);
    }

    public <T> T get(@NotNull String key) {
        if (entryMap.containsKey(key)) {
            return (T) entryMap.get(key).value;
        }
        return null;
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemStoreBuilder builder() {
        return new ItemStoreBuilder(new ConcurrentHashMap<>(entryMap));
    }

}
