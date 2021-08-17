package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ProtocolObject extends Keyed {

    @Contract(pure = true)
    @NotNull NamespaceID namespace();

    @Override
    @Contract(pure = true)
    default @NotNull Key key() {
        return namespace();
    }

    @Contract(pure = true)
    default @NotNull String name() {
        return namespace().asString();
    }

    @Contract(pure = true)
    int id();
}
