package net.minestom.server.registry;

import org.jetbrains.annotations.Contract;

public interface ProtocolObject extends DynamicProtocolObject {

    @Contract(pure = true)
    int id();
}
