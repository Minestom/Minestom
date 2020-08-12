package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.chunk.ChunkUtils;

//todo
public class MultiBlockChangePacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;
    public BlockChange[] blockChanges;

    @Override
    public void write(PacketWriter writer) {
        writer.writeLong(ChunkUtils.getChunkIndex(chunkX, chunkZ));

        if (blockChanges != null) {
            int length = blockChanges.length;
            writer.writeVarInt(length);
            for (int i = 0; i < length; i++) {
                BlockChange blockChange = blockChanges[i];
                writer.writeByte(blockChange.positionXZ);
                writer.writeByte(blockChange.positionY);
                writer.writeVarInt(blockChange.newBlockId);
            }
        } else {
            writer.writeVarInt(0);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.MULTI_BLOCK_CHANGE;
    }

    public static class BlockChange {
        public byte positionXZ;
        public byte positionY;
        public int newBlockId;

    }
}
