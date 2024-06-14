package net.minestom.server.registry;

import org.jetbrains.annotations.Nullable;

public interface ProtocolObject {

    default @Nullable Object registry() {
        return null;
    }
}
