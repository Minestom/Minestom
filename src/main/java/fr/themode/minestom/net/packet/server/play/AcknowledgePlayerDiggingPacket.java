package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status;
    public boolean successful;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(blockPosition);
        writer.writeVarInt(blockStateId);
        writer.writeVarInt(status.ordinal());
        writer.writeBoolean(successful);
    }

    @Override
    public int getId() {
        return 0x8;
    }
}
