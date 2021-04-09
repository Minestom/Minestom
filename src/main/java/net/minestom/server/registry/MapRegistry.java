package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MapRegistry<T extends Keyed> implements Registry.Writable<T> {
    private final Map<Key, T> namespaceToValue = new HashMap<>();

    @Override
    public T get(@NotNull Key id) {
        return namespaceToValue.get(id);
    }

    @Override
    public boolean register(@NotNull T value) {
        if (namespaceToValue.containsKey(value.key())) return false;
        namespaceToValue.put(value.key(), value);
        return true;
    }

    public static class Defaulted<T extends Keyed> extends MapRegistry<T> {
        private final T defaultValue;

        public Defaulted(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public @NotNull T get(@NotNull Key id) {
            final T value = super.get(id);
            return value != null ? value : defaultValue;
        }
    }
}
