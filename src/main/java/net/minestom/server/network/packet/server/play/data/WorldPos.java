package net.minestom.server.network.packet.server.play.data;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record WorldPos(@NotNull String dimension, @NotNull Point blockPosition) implements NetworkBuffer.Writer {
    public static final NetworkBuffer.Type<WorldPos> NETWORK_TYPE = new NetworkBuffer.Type<WorldPos>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, WorldPos value) {
            buffer.write(NetworkBuffer.STRING, value.dimension);
            buffer.write(NetworkBuffer.BLOCK_POSITION, value.blockPosition);
        }

        @Override
        public WorldPos read(@NotNull NetworkBuffer buffer) {
            return new WorldPos(buffer.read(NetworkBuffer.STRING), buffer.read(NetworkBuffer.BLOCK_POSITION));
        }
    };
    public static final BinaryTagSerializer<WorldPos> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new WorldPos(tag.getString("dimension"), BinaryTagSerializer.BLOCK_POSITION.read(tag.get("pos"))),
            pos -> CompoundBinaryTag.builder()
                    .putString("dimension", pos.dimension)
                    .put("pos", BinaryTagSerializer.BLOCK_POSITION.write(pos.blockPosition))
                    .build()
    );

    public WorldPos(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(BLOCK_POSITION));
    }

    public @NotNull WorldPos withDimension(@NotNull String dimension) {
        return new WorldPos(dimension, blockPosition);
    }

    public @NotNull WorldPos withBlockPosition(@NotNull Point blockPosition) {
        return new WorldPos(dimension, blockPosition);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, dimension);
        writer.write(BLOCK_POSITION, blockPosition);
    }

}