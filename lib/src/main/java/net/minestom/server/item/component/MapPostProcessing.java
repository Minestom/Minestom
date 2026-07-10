package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;

public enum MapPostProcessing {
    LOCK,
    SCALE;

    public static final NetworkBuffer.Type<MapPostProcessing> NETWORK_TYPE = NetworkBuffer.Enum(MapPostProcessing.class);
}
