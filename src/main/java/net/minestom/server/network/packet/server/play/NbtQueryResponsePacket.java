package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public record NbtQueryResponsePacket(int transactionId, NBTCompound data) implements ServerPacket {
    public NbtQueryResponsePacket(BinaryReader reader) {
        this(reader.readVarInt(), (NBTCompound) reader.readTag());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(transactionId);
        if (data != null) {
            writer.writeNBT("", data);
        } else {
            // TAG_End
            writer.writeByte((byte) 0x00);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.NBT_QUERY_RESPONSE;
    }
}
