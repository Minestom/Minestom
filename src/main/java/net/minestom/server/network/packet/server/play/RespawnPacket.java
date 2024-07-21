package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record RespawnPacket(
        int dimensionType, @NotNull String worldName,
        long hashedSeed, @NotNull GameMode gameMode, @NotNull GameMode previousGameMode,
        boolean isDebug, boolean isFlat, @Nullable WorldPos deathLocation,
        int portalCooldown, int copyData
) implements ServerPacket.Play {
    public static final int COPY_NONE = 0x0;
    public static final int COPY_ATTRIBUTES = 0x1;
    public static final int COPY_METADATA = 0x2;
    public static final int COPY_ALL = COPY_ATTRIBUTES | COPY_METADATA;

    public RespawnPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(STRING),
                reader.read(LONG), GameMode.fromId(reader.read(BYTE)),
                GameMode.fromId(reader.read(BYTE)),
                reader.read(BOOLEAN), reader.read(BOOLEAN),
                reader.readOptional(WorldPos.NETWORK_TYPE),
                reader.read(VAR_INT), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, dimensionType);
        writer.write(STRING, worldName);
        writer.write(LONG, hashedSeed);
        writer.write(BYTE, gameMode.id());
        writer.write(BYTE, previousGameMode.id());
        writer.write(BOOLEAN, isDebug);
        writer.write(BOOLEAN, isFlat);
        writer.writeOptional(WorldPos.NETWORK_TYPE, deathLocation);
        writer.write(VAR_INT, portalCooldown);
        writer.write(BYTE, (byte) copyData);
    }

}
