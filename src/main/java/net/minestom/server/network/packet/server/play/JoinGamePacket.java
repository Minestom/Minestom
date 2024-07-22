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
        public void write(@NotNull NetworkBuffer writer, JoinGamePacket value) {
            writer.write(INT, value.entityId);
            writer.write(BOOLEAN, value.isHardcore);
            writer.writeCollection(STRING, value.worlds);
            writer.write(VAR_INT, value.maxPlayers);
            writer.write(VAR_INT, value.viewDistance);
            writer.write(VAR_INT, value.simulationDistance);
            writer.write(BOOLEAN, value.reducedDebugInfo);
            writer.write(BOOLEAN, value.enableRespawnScreen);
            writer.write(BOOLEAN, value.doLimitedCrafting);
            writer.write(VAR_INT, value.dimensionType);
            writer.write(STRING, value.world);
            writer.write(LONG, value.hashedSeed);
            writer.write(BYTE, value.gameMode.id());
            if (value.previousGameMode != null) {
                writer.write(BYTE, value.previousGameMode.id());
            } else {
                writer.write(BYTE, (byte) -1);
            }
            writer.write(BOOLEAN, value.isDebug);
            writer.write(BOOLEAN, value.isFlat);
            writer.writeOptional(WorldPos.NETWORK_TYPE, value.deathLocation);
            writer.write(VAR_INT, value.portalCooldown);
            writer.write(BOOLEAN, value.enforcesSecureChat);
        }

        @Override
        public JoinGamePacket read(@NotNull NetworkBuffer reader) {
            return new JoinGamePacket(
                    reader.read(INT),
                    reader.read(BOOLEAN),
                    reader.readCollection(STRING, MAX_WORLDS),
                    reader.read(VAR_INT),
                    reader.read(VAR_INT),
                    reader.read(VAR_INT),
                    reader.read(BOOLEAN),
                    reader.read(BOOLEAN),
                    reader.read(BOOLEAN),
                    reader.read(VAR_INT),
                    reader.read(STRING),
                    reader.read(LONG),
                    GameMode.fromId(reader.read(BYTE)),
                    getNullableGameMode(reader.read(BYTE)),
                    reader.read(BOOLEAN),
                    reader.read(BOOLEAN),
                    reader.readOptional(WorldPos.NETWORK_TYPE),
                    reader.read(VAR_INT),
                    reader.read(BOOLEAN)
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
