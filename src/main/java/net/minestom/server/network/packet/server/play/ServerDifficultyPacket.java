package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ServerDifficultyPacket(@NotNull Difficulty difficulty, boolean locked) implements ServerPacket {
    public ServerDifficultyPacket(@NotNull NetworkBuffer reader) {
        this(Difficulty.values()[reader.read(BYTE)], reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, (byte) difficulty.ordinal());
        writer.write(BOOLEAN, locked);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SERVER_DIFFICULTY;
    }
}
