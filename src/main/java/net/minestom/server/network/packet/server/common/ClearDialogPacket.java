package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record ClearDialogPacket() implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<ClearDialogPacket> SERIALIZER = NetworkBufferTemplate.template(ClearDialogPacket::new);
}
