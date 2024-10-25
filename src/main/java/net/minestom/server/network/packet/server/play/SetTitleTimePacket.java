package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.INT;

public record SetTitleTimePacket(int fadeIn, int stay, int fadeOut) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetTitleTimePacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, SetTitleTimePacket::fadeIn,
            INT, SetTitleTimePacket::stay,
            INT, SetTitleTimePacket::fadeOut,
            SetTitleTimePacket::new);
}
