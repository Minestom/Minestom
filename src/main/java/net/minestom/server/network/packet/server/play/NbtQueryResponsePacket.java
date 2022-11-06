package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import static net.minestom.server.network.NetworkBuffer.*;

public record NbtQueryResponsePacket(int transactionId, NBTCompound data) implements ServerPacket {
    public NbtQueryResponsePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), (NBTCompound) reader.read(NBT));
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

    @Override
    public int getId() {
        return ServerPacketIdentifier.NBT_QUERY_RESPONSE;
    }
}
