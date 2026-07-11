package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.PlayerSpawnInfo;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record JoinGamePacket(
        int entityId, boolean isHardcore, List<String> worlds, int maxPlayers,
        int viewDistance, int simulationDistance, boolean reducedDebugInfo, boolean enableRespawnScreen,
        boolean doLimitedCrafting, PlayerSpawnInfo playerSpawnInfo, boolean onlineMode, boolean enforcesSecureChat
) implements ServerPacket.Play {
    public static final int MAX_WORLDS = Short.MAX_VALUE;
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
            PlayerSpawnInfo.NETWORK_TYPE, JoinGamePacket::playerSpawnInfo,
            BOOLEAN, JoinGamePacket::onlineMode,
            BOOLEAN, JoinGamePacket::enforcesSecureChat,
            JoinGamePacket::new
    );

    public JoinGamePacket {
        worlds = List.copyOf(worlds);
    }
}
