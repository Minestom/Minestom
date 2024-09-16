package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record SetTickStatePacket(float tickRate, boolean isFrozen) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SetTickStatePacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, SetTickStatePacket::tickRate,
            BOOLEAN, SetTickStatePacket::isFrozen,
            SetTickStatePacket::new);
}
