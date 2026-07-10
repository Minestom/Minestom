package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.server.ServerPacket;

public record DebugEventPacket(DebugSubscription.Event<?> event) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugEventPacket> SERIALIZER = NetworkBufferTemplate.template(
            DebugSubscription.Event.NETWORK_TYPE, DebugEventPacket::event,
            DebugEventPacket::new);
}
