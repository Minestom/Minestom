package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;

public record ClientSetBeaconEffectPacket(@Nullable PotionType primaryEffect,
                                          @Nullable PotionType secondaryEffect) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSetBeaconEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            PotionType.NETWORK_TYPE.optional(), ClientSetBeaconEffectPacket::primaryEffect,
            PotionType.NETWORK_TYPE.optional(), ClientSetBeaconEffectPacket::secondaryEffect,
            ClientSetBeaconEffectPacket::new
    );
}
