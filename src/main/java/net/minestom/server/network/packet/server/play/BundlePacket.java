package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record BundlePacket() implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BundlePacket> SERIALIZER = NetworkBufferTemplate.template(BundlePacket::new);
}
