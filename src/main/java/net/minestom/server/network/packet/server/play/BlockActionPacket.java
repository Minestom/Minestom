package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BlockActionPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public byte actionId;
    public byte actionParam;
    public int blockId;

    public BlockActionPacket() {
        blockPosition = new BlockPosition(0,0,0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(actionId);
        writer.writeByte(actionParam);
        writer.writeVarInt(blockId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        blockPosition = reader.readBlockPosition();
        actionId = reader.readByte();
        actionParam = reader.readByte();
        blockId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_ACTION;
    }
}
