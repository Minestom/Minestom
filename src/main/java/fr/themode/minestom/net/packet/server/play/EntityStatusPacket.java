package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class EntityStatusPacket implements ServerPacket {

    public int entityId;
    public byte status;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(entityId);
        writer.writeByte(status);
    }

    @Override
    public int getId() {
        return 0x1C;
    }
}
