package net.minestom.server.network.debug.info;

import net.minestom.server.network.NetworkBuffer;

public enum DebugEntityBlockIntersection {
    IN_BLOCK,
    IN_FLUID,
    IN_AIR;

    public static final NetworkBuffer.Type<DebugEntityBlockIntersection> SERIALIZER = NetworkBuffer.Enum(DebugEntityBlockIntersection.class);
}
