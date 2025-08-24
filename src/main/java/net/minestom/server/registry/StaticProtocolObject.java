package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public non-sealed interface StaticProtocolObject<T> extends RegistryKey<T> {

    @Contract(pure = true)
    default String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    Key key();

    @Contract(pure = true)
    int id();

    default @Nullable Object registry() {
        return null;
    }
}
