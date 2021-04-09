package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class IdCrossMapRegistry<T extends ShortKeyed> extends MapRegistry<T> implements IdCrossRegistry.Writable<T> {
    private final T[] idToValue = (T[]) new Object[Short.MAX_VALUE];

    @Override
    public T get(short id) {
        return idToValue[id];
    }

    @Override
    public boolean register(@NotNull T value) {
        if (!super.register(value))
            return false;
        idToValue[value.getShortId()] = value;
        return true;
    }

    public static class Defaulted<T extends ShortKeyed> extends IdCrossMapRegistry<T> {
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
