package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockEntityType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockEntityDataPacket(
        Point blockPosition,
        BlockEntityType type,
        @Nullable CompoundBinaryTag data
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockEntityDataPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(NetworkBuffer buffer, BlockEntityDataPacket value) {
            buffer.write(BLOCK_POSITION, value.blockPosition);
            buffer.write(BlockEntityType.NETWORK_TYPE, value.type);
            if (value.data != null) {
                buffer.write(NBT_COMPOUND, value.data);
            } else {
                // TAG_End
                buffer.write(BYTE, (byte) 0x00);
            }
        }

        @Override
        public BlockEntityDataPacket read(NetworkBuffer buffer) {
            return new BlockEntityDataPacket(buffer.read(BLOCK_POSITION), buffer.read(BlockEntityType.NETWORK_TYPE), buffer.read(NBT_COMPOUND));
        }
    };

    @Deprecated
    public BlockEntityDataPacket(Point blockPosition, int action, @Nullable CompoundBinaryTag data) {
        this(blockPosition, Objects.requireNonNull(BlockEntityType.fromId(action), "Unknown block entity type"), data);
    }

    @Deprecated
    public int action() {
        return type.id();
    }
}
