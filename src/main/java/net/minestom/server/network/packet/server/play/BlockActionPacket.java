package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record BlockActionPacket(@NotNull Point blockPosition, byte actionId,
                                byte actionParam, int blockId, int sequence) implements ServerPacket {
    public BlockActionPacket(Point blockPosition, byte actionId, byte actionParam, Block block, int sequence) {
        this(blockPosition, actionId, actionParam, block.id(), sequence);
    }

    public BlockActionPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), reader.readByte(),
                reader.readByte(), reader.readVarInt(), reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(actionId);
        writer.writeByte(actionParam);
        writer.writeVarInt(blockId);
        writer.writeVarInt(sequence);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_ACTION;
    }
}
