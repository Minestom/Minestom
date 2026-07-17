package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface StaticProtocolObject<T> extends RegistryKey<T> {

    @Contract(pure = true)
    default String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    Key key();

    @Contract(pure = true)
    int id();

    /**
     * Returns the legacy registry data backing this object.
     *
     * @return the legacy registry data, or {@code null} when none is exposed
     * @deprecated registry values are exposed directly by each protocol object
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    default @Nullable Object registry() {
        return null;
    }
}
