package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.server.ServerPacket;

public record DebugBlockValuePacket(
        Point blockPosition,
        DebugSubscription.Update<?> update
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugBlockValuePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, DebugBlockValuePacket::blockPosition,
            DebugSubscription.Update.NETWORK_TYPE, DebugBlockValuePacket::update,
            DebugBlockValuePacket::new);
}
