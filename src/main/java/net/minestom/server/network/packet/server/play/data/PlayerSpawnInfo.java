package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerSpawnInfo(int dimensionType, String world, long hashedSeed, GameMode gameMode,
                              @Nullable GameMode previousGameMode, boolean debug, boolean flat,
                              @Nullable WorldPos deathLocation, int portalCooldown, int seaLevel) {
    public static final NetworkBuffer.Type<PlayerSpawnInfo> NETWORK_TYPE = NetworkBufferTemplate.template(
            VAR_INT, PlayerSpawnInfo::dimensionType,
            STRING, PlayerSpawnInfo::world,
            LONG, PlayerSpawnInfo::hashedSeed,
            GameMode.NETWORK_TYPE, PlayerSpawnInfo::gameMode,
            GameMode.OPT_NETWORK_TYPE, PlayerSpawnInfo::previousGameMode,
            BOOLEAN, PlayerSpawnInfo::debug,
            BOOLEAN, PlayerSpawnInfo::flat,
            WorldPos.NETWORK_TYPE.optional(), PlayerSpawnInfo::deathLocation,
            VAR_INT, PlayerSpawnInfo::portalCooldown,
            VAR_INT, PlayerSpawnInfo::seaLevel,
            PlayerSpawnInfo::new
    );

    public PlayerSpawnInfo {
        Objects.requireNonNull(gameMode, "gameMode");
    }
}
