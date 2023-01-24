package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record RespawnPacket(String dimensionType, String worldName,
                            long hashedSeed, GameMode gameMode, GameMode previousGameMode,
                            boolean isDebug, boolean isFlat, boolean copyMeta) implements ServerPacket {
    public RespawnPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(STRING),
                reader.read(LONG), GameMode.values()[reader.read(BYTE)], GameMode.values()[reader.read(BYTE)],
                reader.read(BOOLEAN), reader.read(BOOLEAN), reader.read(BOOLEAN));
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
        writer.write(BOOLEAN, copyMeta);

        writer.write(BOOLEAN, false);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESPAWN;
    }
}
