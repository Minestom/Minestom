package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record HitAnimationPacket(int entityId, float yaw) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<HitAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, HitAnimationPacket::entityId,
            FLOAT, HitAnimationPacket::yaw,
            HitAnimationPacket::new);
}
