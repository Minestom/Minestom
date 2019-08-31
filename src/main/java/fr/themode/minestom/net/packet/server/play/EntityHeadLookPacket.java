package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class EntityHeadLookPacket implements ServerPacket {

    public int entityId;
    public float yaw;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) (this.yaw * 256 / 360));
    }

    @Override
    public int getId() {
        return 0x3B;
    }
}
