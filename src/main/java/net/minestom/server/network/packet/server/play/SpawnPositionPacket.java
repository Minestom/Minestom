package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record SpawnPositionPacket(@NotNull Point position, float angle) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, SpawnPositionPacket::position,
            FLOAT, SpawnPositionPacket::angle,
            SpawnPositionPacket::new);
}
