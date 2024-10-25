package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record PlayerRotationPacket(float yaw, float pitch) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<PlayerRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, PlayerRotationPacket::yaw,
            FLOAT, PlayerRotationPacket::pitch,
            PlayerRotationPacket::new);
}
