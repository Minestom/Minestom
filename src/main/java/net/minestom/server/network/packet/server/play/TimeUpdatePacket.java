package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.LONG;

public record TimeUpdatePacket(long worldAge, long timeOfDay, boolean tickDayTime) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<TimeUpdatePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, TimeUpdatePacket::worldAge,
            LONG, TimeUpdatePacket::timeOfDay,
            BOOLEAN, TimeUpdatePacket::tickDayTime,
            TimeUpdatePacket::new);
}
