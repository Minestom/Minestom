package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientPlayerRotationPacket(float yaw, float pitch, boolean onGround) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, ClientPlayerRotationPacket::yaw,
            FLOAT, ClientPlayerRotationPacket::pitch,
            BOOLEAN, ClientPlayerRotationPacket::onGround,
            ClientPlayerRotationPacket::new);
}
