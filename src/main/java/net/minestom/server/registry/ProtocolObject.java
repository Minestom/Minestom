package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public interface ProtocolObject extends Keyed {

    @NotNull NamespaceID namespace();

    @Override
    default @NotNull Key key() {
        return namespace();
    }

    default @NotNull String name() {
        return namespace().asString();
    }

    int id();
}
