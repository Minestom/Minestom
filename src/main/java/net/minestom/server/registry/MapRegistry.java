package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MapRegistry<T extends Keyed> implements IRegistry.Writable<T> {
    private final Map<Key, T> namespaceToValue = new ConcurrentHashMap<>();

    @Override
    public T get(@NotNull Key id) {
        return namespaceToValue.get(id);
    }

    @Override
    @NotNull
    public List<T> values() {
        return new ArrayList<>(namespaceToValue.values());
    }

    @Override
    public boolean register(@NotNull T value) {
        if (namespaceToValue.containsKey(value.key())) {
            return false;
        } else if (namespaceToValue.containsValue(value)) {
            return false;
        }
        namespaceToValue.put(value.key(), value);
        return true;
    }

    public static class Defaulted<T extends Keyed> extends MapRegistry<T> {
        private final Supplier<T> defaultValue;

        public Defaulted(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public @NotNull T get(@NotNull Key id) {
            final T value = super.get(id);
            return value != null ? value : defaultValue.get();
        }
    }
}
