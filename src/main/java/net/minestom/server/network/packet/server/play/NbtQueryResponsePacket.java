package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record NbtQueryResponsePacket(int transactionId, CompoundBinaryTag data) implements ServerPacket.Play {
    public NbtQueryResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), (CompoundBinaryTag) reader.read(NBT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, transactionId);
        if (data != null) {
            writer.write(NBT, data);
        } else {
            // TAG_End
            writer.write(BYTE, (byte) 0x00);
        }
    }

}
