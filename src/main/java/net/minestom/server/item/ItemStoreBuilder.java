package net.minestom.server.item;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemStoreBuilder {

    private final Map<String, Entry<?>> entryMap;

    protected ItemStoreBuilder(@NotNull Map<String, Entry<?>> entryMap) {
        this.entryMap = entryMap;
    }

    protected ItemStoreBuilder() {
        this(new ConcurrentHashMap<>());
    }

    @Contract(value = "_, _, _ -> this")
    public <T> @NotNull ItemStoreBuilder set(@NotNull String key, T value, MergingRule<T> mergingRule) {
        this.entryMap.put(key, new Entry<>(value, mergingRule));
        return this;
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull ItemStore build() {
        return new ItemStore(new HashMap<>(entryMap));
    }

    public interface MergingRule<T> {
        @Nullable T apply(@NotNull T value1, @NotNull T value2);
    }

    protected static class Entry<T> {
        protected final T value;
        protected final MergingRule<T> mergingRule;

        private Entry(@NotNull T value, @NotNull MergingRule<T> mergingRule) {
            this.value = value;
            this.mergingRule = mergingRule;
        }
    }

}
