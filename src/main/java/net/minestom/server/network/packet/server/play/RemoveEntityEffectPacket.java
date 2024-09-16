package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record RemoveEntityEffectPacket(int entityId, @NotNull PotionEffect potionEffect) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<RemoveEntityEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, RemoveEntityEffectPacket::entityId,
            PotionEffect.NETWORK_TYPE, RemoveEntityEffectPacket::potionEffect,
            RemoveEntityEffectPacket::new
    );
}
