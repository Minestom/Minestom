package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetCooldownPacket(int itemId, int cooldownTicks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetCooldownPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SetCooldownPacket::itemId,
            VAR_INT, SetCooldownPacket::cooldownTicks,
            SetCooldownPacket::new);
}
