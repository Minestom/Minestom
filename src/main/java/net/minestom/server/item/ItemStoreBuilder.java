package net.minestom.server.item;

import it.unimi.dsi.fastutil.Pair;
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
    public synchronized <T> @NotNull ItemStoreBuilder set(@NotNull String key, T value, MergingRule<T> mergingRule) {
        this.entryMap.put(key, new Entry<>(value, mergingRule));
        return this;
    }

    @Contract(value = "-> new", pure = true)
    public synchronized @NotNull ItemStore build() {
        return new ItemStore(new HashMap<>(entryMap));
    }

    @Contract(value = "_ -> this", pure = true)
    public @NotNull ItemStoreBuilder merge(@NotNull ItemStoreBuilder builder) {
        if (hashCode() <= builder.hashCode()) {
            merge0(builder, this.entryMap);
        } else {
            builder.merge0(this, this.entryMap);
        }
        return this;
    }

    private synchronized void merge0(@NotNull ItemStoreBuilder builder, Map<String, Entry<?>> entries) {
        synchronized (builder) {
            Map<String, Entry<?>> result = new HashMap<>();
            this.entryMap.forEach((key, entry) -> {
                var otherEntry = builder.entryMap.get(key);
                if (otherEntry == null) {
                    result.put(key, entry);
                } else {
                    result.put(key, entry.merge(otherEntry));
                }
            });
            builder.entryMap.entrySet().stream()
                    .filter(entry -> !this.entryMap.containsKey(entry.getKey()))
                    .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            entries.clear();
            entries.putAll(result);
        }
    }

    @Contract(value = "_, _ -> new", pure = true)
    public synchronized @NotNull Pair<@NotNull ItemStoreBuilder, @NotNull ItemStoreBuilder> split(int firstAmount, int secondAmount) {
        Map<String, Entry<?>> result = new HashMap<>();
        var first = new ItemStoreBuilder();
        var second = new ItemStoreBuilder();
        this.entryMap.forEach((key, entry) -> {
            var entries = entry.split(firstAmount, secondAmount);
            if (entries.left() != null) {
                first.entryMap.put(key, entries.left());
            }
            if (entries.right() != null) {
                second.entryMap.put(key, entries.right());
            }
        });
        return Pair.of(first, second);
    }

    public interface MergingRule<T> {
        @Nullable T merge(@NotNull T value1, @NotNull T value2);
        @NotNull Pair<@Nullable T, @Nullable T> split(@NotNull T value, int firstAmount, int secondAmount);
    }

    protected static class Entry<T> {
        protected final T value;
        protected final MergingRule<T> mergingRule;

        private Entry(@NotNull T value, @NotNull MergingRule<T> mergingRule) {
            this.value = value;
            this.mergingRule = mergingRule;
        }

        @SuppressWarnings("unchecked")
        private @Nullable Entry<T> merge(Entry<?> other) {
            var casted = (Entry<T>) other;
            var newValue = this.mergingRule.merge(this.value, casted.value);
            return newValue == null ? null : new Entry<>(newValue, this.mergingRule);
        }

        @SuppressWarnings("ConstantConditions")
        private @NotNull Pair<@Nullable Entry<T>, @Nullable Entry<T>> split(int firstAmount, int secondAmount) {
            var values = this.mergingRule.split(this.value, firstAmount, secondAmount);
            return Pair.of(
                    values.left() == null ? null : new Entry<>(values.left(), this.mergingRule),
                    values.right() == null ? null : new Entry<>(values.right(), this.mergingRule)
            );
        }

    }

}
