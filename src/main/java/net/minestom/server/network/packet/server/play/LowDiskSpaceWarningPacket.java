package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record LowDiskSpaceWarningPacket() implements ServerPacket.Play {
    public static final NetworkBuffer.Type<LowDiskSpaceWarningPacket> SERIALIZER = NetworkBufferTemplate.template(new LowDiskSpaceWarningPacket());
}
