package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record WorldPos(@NotNull String dimension, @NotNull Point position) implements NetworkBuffer.Writer {

    public WorldPos(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(BLOCK_POSITION));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, dimension);
        writer.write(BLOCK_POSITION, position);
    }

}