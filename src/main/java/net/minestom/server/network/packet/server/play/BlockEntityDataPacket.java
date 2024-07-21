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
    public BlockEntityDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.read(VAR_INT), (CompoundBinaryTag) reader.read(NBT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(VAR_INT, action);
        if (data != null) {
            writer.write(NBT, data);
        } else {
            // TAG_End
            writer.write(BYTE, (byte) 0x00);
        }
    }

}
