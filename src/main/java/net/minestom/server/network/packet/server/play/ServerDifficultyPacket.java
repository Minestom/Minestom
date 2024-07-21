package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ServerDifficultyPacket(@NotNull Difficulty difficulty, boolean locked) implements ServerPacket.Play {
    public ServerDifficultyPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Difficulty.class), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Difficulty.class, difficulty);
        writer.write(BOOLEAN, locked);
    }

}
