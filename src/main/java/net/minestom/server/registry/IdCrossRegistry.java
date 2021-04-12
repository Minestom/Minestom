package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IdCrossRegistry<T extends Keyed> extends Registry<T> {
    @Nullable
    T get(short id);

    short getId(T obj);

    interface Writable<T extends Keyed> extends IdCrossRegistry<T>, Registry.Writable<T> {
        boolean register(short id, @NotNull T value);
    }
}
