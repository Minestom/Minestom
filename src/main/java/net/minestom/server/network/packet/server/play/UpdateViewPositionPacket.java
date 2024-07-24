package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateViewPositionPacket(int chunkX, int chunkZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UpdateViewPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, UpdateViewPositionPacket::chunkX,
            VAR_INT, UpdateViewPositionPacket::chunkZ,
            UpdateViewPositionPacket::new);
}
