package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

record RegistryKeyImpl<T>(@NotNull Key key) implements RegistryKey<T> {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegistryKey<?> that)) return false;
        return Objects.equals(key, that.key());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

}
