package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record JoinGamePacket(
        int entityId, boolean isHardcore, List<String> worlds, int maxPlayers,
        int viewDistance, int simulationDistance, boolean reducedDebugInfo, boolean enableRespawnScreen,
        boolean doLimitedCrafting, int dimensionType,
        String world, long hashedSeed, GameMode gameMode, GameMode previousGameMode,
        boolean isDebug, boolean isFlat, @Nullable WorldPos deathLocation, int portalCooldown,
        int seaLevel, boolean enforcesSecureChat
) implements ServerPacket.Play {
    public static final int MAX_WORLDS = Short.MAX_VALUE;

    public JoinGamePacket {
        worlds = List.copyOf(worlds);
    }

    public static final NetworkBuffer.Type<JoinGamePacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, JoinGamePacket::entityId,
            BOOLEAN, JoinGamePacket::isHardcore,
            STRING.list(MAX_WORLDS), JoinGamePacket::worlds,
            VAR_INT, JoinGamePacket::maxPlayers,
            VAR_INT, JoinGamePacket::viewDistance,
            VAR_INT, JoinGamePacket::simulationDistance,
            BOOLEAN, JoinGamePacket::reducedDebugInfo,
            BOOLEAN, JoinGamePacket::enableRespawnScreen,
            BOOLEAN, JoinGamePacket::doLimitedCrafting,
            VAR_INT, JoinGamePacket::dimensionType,
            STRING, JoinGamePacket::world,
            LONG, JoinGamePacket::hashedSeed,
            GameMode.NETWORK_TYPE, JoinGamePacket::gameMode,
            GameMode.OPT_NETWORK_TYPE, JoinGamePacket::previousGameMode,
            BOOLEAN, JoinGamePacket::isDebug,
            BOOLEAN, JoinGamePacket::isFlat,
            WorldPos.NETWORK_TYPE.optional(), JoinGamePacket::deathLocation,
            VAR_INT, JoinGamePacket::portalCooldown,
            VAR_INT, JoinGamePacket::seaLevel,
            BOOLEAN, JoinGamePacket::enforcesSecureChat,
            JoinGamePacket::new
    );
}
