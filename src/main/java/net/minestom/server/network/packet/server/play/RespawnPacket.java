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
        public void write(@NotNull NetworkBuffer buffer, @NotNull RespawnPacket value) {
            buffer.write(VAR_INT, value.dimensionType);
            buffer.write(STRING, value.worldName);
            buffer.write(LONG, value.hashedSeed);
            buffer.write(BYTE, value.gameMode.id());
            buffer.write(BYTE, value.previousGameMode.id());
            buffer.write(BOOLEAN, value.isDebug);
            buffer.write(BOOLEAN, value.isFlat);
            buffer.writeOptional(value.deathLocation);
            buffer.write(VAR_INT, value.portalCooldown);
            buffer.write(BYTE, (byte) value.copyData);
        }

        @Override
        public @NotNull RespawnPacket read(@NotNull NetworkBuffer buffer) {
            return new RespawnPacket(buffer.read(VAR_INT), buffer.read(STRING),
                    buffer.read(LONG), GameMode.fromId(buffer.read(BYTE)),
                    GameMode.fromId(buffer.read(BYTE)),
                    buffer.read(BOOLEAN), buffer.read(BOOLEAN),
                    buffer.readOptional(WorldPos.NETWORK_TYPE),
                    buffer.read(VAR_INT), buffer.read(BYTE));
        }
    };
}
