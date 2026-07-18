package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.PlayerSpawnInfo;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record RespawnPacket(PlayerSpawnInfo playerSpawnInfo, byte copyData) implements ServerPacket.Play {
    public static final int COPY_NONE = 0x0;
    public static final int COPY_ATTRIBUTES = 0x1;
    public static final int COPY_METADATA = 0x2;
    public static final int COPY_ALL = COPY_ATTRIBUTES | COPY_METADATA;

    public static final NetworkBuffer.Type<RespawnPacket> SERIALIZER = NetworkBufferTemplate.template(
            PlayerSpawnInfo.NETWORK_TYPE, RespawnPacket::playerSpawnInfo,
            BYTE, RespawnPacket::copyData,
            RespawnPacket::new);

}
