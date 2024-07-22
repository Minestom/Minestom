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

    public static final NetworkBuffer.Type<RespawnPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, @NotNull RespawnPacket value) {
            writer.write(VAR_INT, value.dimensionType);
            writer.write(STRING, value.worldName);
            writer.write(LONG, value.hashedSeed);
            writer.write(BYTE, value.gameMode.id());
            writer.write(BYTE, value.previousGameMode.id());
            writer.write(BOOLEAN, value.isDebug);
            writer.write(BOOLEAN, value.isFlat);
            writer.writeOptional(value.deathLocation);
            writer.write(VAR_INT, value.portalCooldown);
            writer.write(BYTE, (byte) value.copyData);
        }

        @Override
        public @NotNull RespawnPacket read(@NotNull NetworkBuffer reader) {
            return new RespawnPacket(reader.read(VAR_INT), reader.read(STRING),
                    reader.read(LONG), GameMode.fromId(reader.read(BYTE)),
                    GameMode.fromId(reader.read(BYTE)),
                    reader.read(BOOLEAN), reader.read(BOOLEAN),
                    reader.readOptional(WorldPos.NETWORK_TYPE),
                    reader.read(VAR_INT), reader.read(BYTE));
        }
    };
}
