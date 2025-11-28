package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateHealthPacket(float health, int food, float foodSaturation) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateHealthPacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, UpdateHealthPacket::health,
            VAR_INT, UpdateHealthPacket::food,
            FLOAT, UpdateHealthPacket::foodSaturation,
            UpdateHealthPacket::new);
}
