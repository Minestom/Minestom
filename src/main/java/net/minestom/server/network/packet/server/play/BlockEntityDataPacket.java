package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record BlockEntityDataPacket(@NotNull Point blockPosition, int action,
                                    @Nullable CompoundBinaryTag data) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<BlockEntityDataPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BlockEntityDataPacket value) {
            buffer.write(BLOCK_POSITION, value.blockPosition);
            buffer.write(VAR_INT, value.action);
            if (value.data != null) {
                buffer.write(NBT, value.data);
            } else {
                // TAG_End
                buffer.write(BYTE, (byte) 0x00);
            }
        }

        @Override
        public BlockEntityDataPacket read(@NotNull NetworkBuffer buffer) {
            return new BlockEntityDataPacket(buffer.read(BLOCK_POSITION), buffer.read(VAR_INT), (CompoundBinaryTag) buffer.read(NBT));
        }
    };
}
