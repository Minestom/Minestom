package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;

public class BlockChangePacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockId;

    @Override
    public void write(Buffer buffer) {
        Utils.writePosition(buffer, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        Utils.writeVarInt(buffer, blockId);
    }

    @Override
    public int getId() {
        return 0x0B;
    }
}
