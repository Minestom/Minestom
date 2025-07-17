package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record NbtQueryResponsePacket(int transactionId, CompoundBinaryTag data) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<NbtQueryResponsePacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, NbtQueryResponsePacket value) {
            buffer.write(VAR_INT, value.transactionId);
            if (value.data != null) {
                buffer.write(NBT_COMPOUND, value.data);
            } else {
                // TAG_End
                buffer.write(BYTE, (byte) 0x00);
            }
        }

        @Override
        public NbtQueryResponsePacket read(@NotNull NetworkBuffer buffer) {
            return new NbtQueryResponsePacket(buffer.read(VAR_INT), buffer.read(NBT_COMPOUND));
        }
    };
}
