package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record RespawnPacket(
        int dimensionType, @NotNull String worldName,
        long hashedSeed, @NotNull GameMode gameMode, @NotNull GameMode previousGameMode,
        boolean isDebug, boolean isFlat, @Nullable WorldPos deathLocation,
        int portalCooldown, byte copyData, int seaLevel
) implements ServerPacket.Play {
    public static final int COPY_NONE = 0x0;
    public static final int COPY_ATTRIBUTES = 0x1;
    public static final int COPY_METADATA = 0x2;
    public static final int COPY_ALL = COPY_ATTRIBUTES | COPY_METADATA;

    public static final NetworkBuffer.Type<RespawnPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, RespawnPacket::dimensionType,
            STRING, RespawnPacket::worldName,
            LONG, RespawnPacket::hashedSeed,
            GameMode.NETWORK_TYPE, RespawnPacket::gameMode,
            GameMode.OPT_NETWORK_TYPE, RespawnPacket::previousGameMode,
            BOOLEAN, RespawnPacket::isDebug,
            BOOLEAN, RespawnPacket::isFlat,
            WorldPos.NETWORK_TYPE.optional(), RespawnPacket::deathLocation,
            VAR_INT, RespawnPacket::portalCooldown,
            BYTE, RespawnPacket::copyData,
            VAR_INT, RespawnPacket::seaLevel,
            RespawnPacket::new);
}
