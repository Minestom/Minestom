package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;

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
        return 0x0A;
    }
}
