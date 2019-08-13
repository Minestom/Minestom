package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class MultiBlockChangePacket implements ServerPacket {

    public int chunkX;
    public int chunkZ;
    public BlockChange[] blockChanges;

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(chunkX);
        buffer.putInt(chunkZ);
        Utils.writeVarInt(buffer, blockChanges == null ? 0 : blockChanges.length);

        if (blockChanges != null) {
            for (int i = 0; i < blockChanges.length; i++) {
                BlockChange blockChange = blockChanges[i];
                buffer.putByte(blockChange.positionXZ);
                buffer.putByte(blockChange.positionY);
                Utils.writeVarInt(buffer, blockChange.newBlockId);
            }
        }
    }

    @Override
    public int getId() {
        return 0x0F;
    }

    public static class BlockChange {
        public byte positionXZ;
        public byte positionY;
        public int newBlockId;

    }
}
