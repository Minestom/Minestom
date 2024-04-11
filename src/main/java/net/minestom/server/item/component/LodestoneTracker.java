package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record LodestoneTracker(@NotNull String dimension, @NotNull Point blockPosition, boolean tracked) {

    public static final NetworkBuffer.Type<LodestoneTracker> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull LodestoneTracker value) {
            buffer.write(NetworkBuffer.STRING, value.dimension);
            buffer.write(NetworkBuffer.BLOCK_POSITION, value.blockPosition);
            buffer.write(NetworkBuffer.BOOLEAN, value.tracked);
        }

        @Override
        public @NotNull LodestoneTracker read(@NotNull NetworkBuffer buffer) {
            return new LodestoneTracker(
                buffer.read(NetworkBuffer.STRING),
                buffer.read(NetworkBuffer.BLOCK_POSITION),
                buffer.read(NetworkBuffer.BOOLEAN)
            );
        }
    };

    public static final BinaryTagSerializer<LodestoneTracker> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull LodestoneTracker value) {
            throw new UnsupportedOperationException("Not implemented"); //todo
        }

        @Override
        public @NotNull LodestoneTracker read(@NotNull BinaryTag tag) {
            throw new UnsupportedOperationException("Not implemented"); //todo
        }
    };

    public @NotNull LodestoneTracker withDimension(@NotNull String dimension) {
        return new LodestoneTracker(dimension, blockPosition, tracked);
    }

    public @NotNull LodestoneTracker withBlockPosition(@NotNull Point blockPosition) {
        return new LodestoneTracker(dimension, blockPosition, tracked);
    }

    public @NotNull LodestoneTracker withTracked(boolean tracked) {
        return new LodestoneTracker(dimension, blockPosition, tracked);
    }

}
