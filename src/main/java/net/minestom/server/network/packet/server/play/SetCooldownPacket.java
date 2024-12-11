package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record SetCooldownPacket(@NotNull String cooldownGroup, int cooldownTicks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetCooldownPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, SetCooldownPacket::cooldownGroup,
            VAR_INT, SetCooldownPacket::cooldownTicks,
            SetCooldownPacket::new);
}
