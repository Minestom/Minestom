package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record JoinGamePacket(
        int entityId, boolean isHardcore, List<String> worlds, int maxPlayers,
        int viewDistance, int simulationDistance, boolean reducedDebugInfo, boolean enableRespawnScreen,
        boolean doLimitedCrafting, int dimensionType,
        String world, long hashedSeed, GameMode gameMode, GameMode previousGameMode,
        boolean isDebug, boolean isFlat, @Nullable WorldPos deathLocation, int portalCooldown,
        boolean enforcesSecureChat
) implements ServerPacket.Play {
    public static final int MAX_WORLDS = Short.MAX_VALUE;

    public JoinGamePacket {
        worlds = List.copyOf(worlds);
    }

    public static final NetworkBuffer.Type<JoinGamePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, JoinGamePacket value) {
            buffer.write(INT, value.entityId);
            buffer.write(BOOLEAN, value.isHardcore);
            buffer.writeCollection(STRING, value.worlds);
            buffer.write(VAR_INT, value.maxPlayers);
            buffer.write(VAR_INT, value.viewDistance);
            buffer.write(VAR_INT, value.simulationDistance);
            buffer.write(BOOLEAN, value.reducedDebugInfo);
            buffer.write(BOOLEAN, value.enableRespawnScreen);
            buffer.write(BOOLEAN, value.doLimitedCrafting);
            buffer.write(VAR_INT, value.dimensionType);
            buffer.write(STRING, value.world);
            buffer.write(LONG, value.hashedSeed);
            buffer.write(BYTE, value.gameMode.id());
            if (value.previousGameMode != null) {
                buffer.write(BYTE, value.previousGameMode.id());
            } else {
                buffer.write(BYTE, (byte) -1);
            }
            buffer.write(BOOLEAN, value.isDebug);
            buffer.write(BOOLEAN, value.isFlat);
            buffer.writeOptional(WorldPos.NETWORK_TYPE, value.deathLocation);
            buffer.write(VAR_INT, value.portalCooldown);
            buffer.write(BOOLEAN, value.enforcesSecureChat);
        }

        @Override
        public JoinGamePacket read(@NotNull NetworkBuffer buffer) {
            return new JoinGamePacket(
                    buffer.read(INT),
                    buffer.read(BOOLEAN),
                    buffer.readCollection(STRING, MAX_WORLDS),
                    buffer.read(VAR_INT),
                    buffer.read(VAR_INT),
                    buffer.read(VAR_INT),
                    buffer.read(BOOLEAN),
                    buffer.read(BOOLEAN),
                    buffer.read(BOOLEAN),
                    buffer.read(VAR_INT),
                    buffer.read(STRING),
                    buffer.read(LONG),
                    GameMode.fromId(buffer.read(BYTE)),
                    getNullableGameMode(buffer.read(BYTE)),
                    buffer.read(BOOLEAN),
                    buffer.read(BOOLEAN),
                    buffer.readOptional(WorldPos.NETWORK_TYPE),
                    buffer.read(VAR_INT),
                    buffer.read(BOOLEAN)
            );
        }
    };

    /**
     * This method exists in lieu of a NetworkBufferType since -1 is only a
     * valid value in this packet and changing behaviour of GameMode.fromId()
     * to be nullable would be too big of a change. Also, game modes are often
     * represented as other data types, including floats.
     */
    private static @Nullable GameMode getNullableGameMode(final byte id) {
        return id == (byte) -1 ? null : GameMode.fromId(id);
    }
}
