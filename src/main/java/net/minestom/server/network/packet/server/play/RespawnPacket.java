package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.play.data.DeathLocation;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record RespawnPacket(
        String dimensionType, String worldName,
        long hashedSeed, GameMode gameMode, GameMode previousGameMode,
        boolean isDebug, boolean isFlat, DeathLocation deathLocation,
        int portalCooldown, int copyData
) implements ServerPacket {
    public static final int COPY_NONE = 0x0;
    public static final int COPY_ATTRIBUTES = 0x1;
    public static final int COPY_METADATA = 0x2;
    public static final int COPY_ALL = COPY_ATTRIBUTES | COPY_METADATA;

    public RespawnPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(STRING),
                reader.read(LONG), GameMode.fromId(reader.read(BYTE)),
                GameMode.fromId(reader.read(BYTE)),
                reader.read(BOOLEAN), reader.read(BOOLEAN),
                reader.read(DEATH_LOCATION),
                reader.read(VAR_INT), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, dimensionType);
        writer.write(STRING, worldName);
        writer.write(LONG, hashedSeed);
        writer.write(BYTE, gameMode.id());
        writer.write(BYTE, previousGameMode.id());
        writer.write(BOOLEAN, isDebug);
        writer.write(BOOLEAN, isFlat);
        writer.write(DEATH_LOCATION, deathLocation);
        writer.write(VAR_INT, portalCooldown);
        writer.write(BYTE, (byte) copyData);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.RESPAWN;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
