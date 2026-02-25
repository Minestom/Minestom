package net.minestom.server.network.debug.info;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record DebugHiveInfo(
        Block type,
        int occupantCount,
        int honeyLevel,
        boolean sedated
) {
    public static final NetworkBuffer.Type<DebugHiveInfo> SERIALIZER = NetworkBufferTemplate.template(
            Block.ID_NETWORK_TYPE, DebugHiveInfo::type,
            NetworkBuffer.INT, DebugHiveInfo::occupantCount,
            NetworkBuffer.INT, DebugHiveInfo::honeyLevel,
            NetworkBuffer.BOOLEAN, DebugHiveInfo::sedated,
            DebugHiveInfo::new);
}
