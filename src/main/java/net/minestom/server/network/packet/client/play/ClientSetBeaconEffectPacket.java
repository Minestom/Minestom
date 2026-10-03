package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

public record ClientSetBeaconEffectPacket(@Nullable PotionEffect primaryEffect,
                                          @Nullable PotionEffect secondaryEffect) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientSetBeaconEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            PotionEffect.NETWORK_TYPE.optional(), ClientSetBeaconEffectPacket::primaryEffect,
            PotionEffect.NETWORK_TYPE.optional(), ClientSetBeaconEffectPacket::secondaryEffect,
            ClientSetBeaconEffectPacket::new
    );
}
