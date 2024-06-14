package net.minestom.server.registry;

import org.jetbrains.annotations.Contract;

public interface StaticProtocolObject extends ProtocolObject {

    @Contract(pure = true)
    int id();
}
