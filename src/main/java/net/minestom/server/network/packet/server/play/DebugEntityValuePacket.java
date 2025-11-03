package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.server.ServerPacket;

public record DebugEntityValuePacket(int entityId, DebugSubscription.Update<?> update) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugEntityValuePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, DebugEntityValuePacket::entityId,
            DebugSubscription.Update.NETWORK_TYPE, DebugEntityValuePacket::update,
            DebugEntityValuePacket::new);
}
