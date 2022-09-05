package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record AcknowledgeBlockChangePacket(int sequence) implements ServerPacket {
    public AcknowledgeBlockChangePacket(BinaryReader reader) {
        this(reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(sequence);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACKNOWLEDGE_BLOCK_CHANGE;
    }
}
