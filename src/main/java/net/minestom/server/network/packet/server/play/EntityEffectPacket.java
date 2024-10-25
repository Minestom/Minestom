package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityEffectPacket(int entityId, @NotNull Potion potion) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityEffectPacket::entityId,
            Potion.NETWORK_TYPE, EntityEffectPacket::potion,
            EntityEffectPacket::new
    );
}
