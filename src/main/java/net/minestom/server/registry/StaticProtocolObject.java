package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface StaticProtocolObject extends ProtocolObject, Keyed {

    @Contract(pure = true)
    @NotNull Key namespace();

    @Contract(pure = true)
    default @NotNull String name() {
        return namespace().asString();
    }

    @Override
    @Contract(pure = true)
    default @NotNull Key key() {
        return namespace();
    }

    @Contract(pure = true)
    int id();
}
