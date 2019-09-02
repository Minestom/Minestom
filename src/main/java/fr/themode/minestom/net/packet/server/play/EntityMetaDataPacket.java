package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import simplenet.packet.Packet;

import java.util.function.Consumer;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Consumer<Packet> consumer;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.write(consumer);
        writer.writeByte((byte) 0xFF);
    }

    @Override
    public int getId() {
        return 0x43;
    }
}
