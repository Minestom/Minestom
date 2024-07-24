package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerPositionAndLookPacket(Pos position, byte flags, int teleportId) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<PlayerPositionAndLookPacket> SERIALIZER = NetworkBufferTemplate.template(
            POS, PlayerPositionAndLookPacket::position,
            BYTE, PlayerPositionAndLookPacket::flags,
            VAR_INT, PlayerPositionAndLookPacket::teleportId,
            PlayerPositionAndLookPacket::new);
}