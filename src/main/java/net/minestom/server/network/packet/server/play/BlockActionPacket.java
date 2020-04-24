package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.BlockPosition;

public class BlockActionPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public byte actionId;
    public byte actionParam;
    public int blockId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeByte(actionId);
        writer.writeByte(actionParam);
        writer.writeVarInt(blockId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.BLOCK_ACTION;
    }
}
