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

    public JoinGamePacket(@NotNull NetworkBuffer reader) {
        this(
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

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, entityId);
        writer.write(BOOLEAN, isHardcore);
        writer.writeCollection(STRING, worlds);
        writer.write(VAR_INT, maxPlayers);
        writer.write(VAR_INT, viewDistance);
        writer.write(VAR_INT, simulationDistance);
        writer.write(BOOLEAN, reducedDebugInfo);
        writer.write(BOOLEAN, enableRespawnScreen);
        writer.write(BOOLEAN, doLimitedCrafting);
        writer.write(VAR_INT, dimensionType);
        writer.write(STRING, world);
        writer.write(LONG, hashedSeed);
        writer.write(BYTE, gameMode.id());
        if (previousGameMode != null) {
            writer.write(BYTE, previousGameMode.id());
        } else {
            writer.write(BYTE, (byte) -1);
        }
        writer.write(BOOLEAN, isDebug);
        writer.write(BOOLEAN, isFlat);
        writer.writeOptional(WorldPos.NETWORK_TYPE, deathLocation);
        writer.write(VAR_INT, portalCooldown);
        writer.write(BOOLEAN, enforcesSecureChat);
    }

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
