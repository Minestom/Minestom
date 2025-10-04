package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DebugBeeInfo(
        @Nullable Point hivePosition,
        @Nullable Point flowerPosition,
        int travelTicks,
        List<Point> blacklistedHives
) {
    public static NetworkBuffer.Type<DebugBeeInfo> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION.optional(), DebugBeeInfo::hivePosition,
            NetworkBuffer.BLOCK_POSITION.optional(), DebugBeeInfo::flowerPosition,
            NetworkBuffer.VAR_INT, DebugBeeInfo::travelTicks,
            NetworkBuffer.BLOCK_POSITION.list(), DebugBeeInfo::blacklistedHives,
            DebugBeeInfo::new);
}
