package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Buffer data;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeBuffer(data);
        writer.writeByte((byte) 0xFF);
    }

    @Override
    public int getId() {
        return 0x43;
    }
}
