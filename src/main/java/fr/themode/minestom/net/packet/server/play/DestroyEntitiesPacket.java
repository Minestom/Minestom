package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public int getId() {
        return 0x37;
    }
}
