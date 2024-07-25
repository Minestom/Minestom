package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface StaticProtocolObject extends ProtocolObject, Keyed {

    @Contract(pure = true)
    default @NotNull String name() {
        return key().asString();
    }

    @Contract(pure = true)
    int id();
}
