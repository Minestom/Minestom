package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;

public class BlockChangePacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockId);
    }

    @Override
    public int getId() {
        return 0x0B;
    }
}
