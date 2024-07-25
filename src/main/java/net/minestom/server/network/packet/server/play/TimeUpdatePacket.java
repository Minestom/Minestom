package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record TimeUpdatePacket(long worldAge, long timeOfDay) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<TimeUpdatePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, TimeUpdatePacket::worldAge,
            LONG, TimeUpdatePacket::timeOfDay,
            TimeUpdatePacket::new);
}
