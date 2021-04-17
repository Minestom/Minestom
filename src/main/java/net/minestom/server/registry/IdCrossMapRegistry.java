package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class IdCrossMapRegistry<T extends Keyed> extends MapRegistry<T> implements IdCrossRegistry.Writable<T> {
    protected final List<T> values = Collections.synchronizedList(new ArrayList<>(Short.MAX_VALUE));

    @Override
    @Nullable
    public T get(short id) {
        try {
            return values.get(id);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public short getId(T obj) {
        return (short) values.indexOf(obj);
    }

    @Override
    public boolean register(short id, @NotNull T value) {
        if (!super.register(value)) {
            return false;
        } else if (values.contains(value)) {
            return false;
        } else if (values.get(id) != null) {
            return false;
        }
        values.add(id, value);
        return true;
    }

    @Override
    public boolean register(@NotNull T value) {
        if (!super.register(value)) {
            return false;
        } else if (values.contains(value)) {
            return false;
        }
        // We don't care about the id of this time just take the next possible Id.
        values.add(value);
        return true;
    }

    public static class Defaulted<T extends Keyed> extends IdCrossMapRegistry<T> {
        private final Supplier<T> defaultValue;

        public Defaulted(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        @NotNull
        public T get(short id) {
            try {
                return values.get(id);
            } catch (IndexOutOfBoundsException e) {
                return defaultValue.get();
            }
        }

        @Override
        public @NotNull T get(@NotNull Key id) {
            final T value = super.get(id);
            return value != null ? value : defaultValue.get();
        }
    }
}
