package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public interface ProtocolObject extends Keyed {

    @NotNull NamespaceID getNamespaceId();

    @Override
    default @NotNull Key key() {
        return getNamespaceId();
    }

    default @NotNull String getName() {
        return getNamespaceId().asString();
    }

    int getId();
}
