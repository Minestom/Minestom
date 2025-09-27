package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.WorldPos;

import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record SpawnPositionPacket(WorldPos worldPos, float yaw, float pitch) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            WorldPos.NETWORK_TYPE, SpawnPositionPacket::worldPos,
            FLOAT, SpawnPositionPacket::yaw,
            FLOAT, SpawnPositionPacket::pitch,
            SpawnPositionPacket::new);
}
