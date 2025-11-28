package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

public record DebugBreezeInfo(
        @Nullable Integer attackTarget,
        @Nullable Point jumpTarget
) {
    public static NetworkBuffer.Type<DebugBreezeInfo> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT.optional(), DebugBreezeInfo::attackTarget,
            NetworkBuffer.BLOCK_POSITION.optional(), DebugBreezeInfo::jumpTarget,
            DebugBreezeInfo::new);
}
