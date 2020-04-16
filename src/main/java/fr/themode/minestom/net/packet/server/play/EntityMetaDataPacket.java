package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

import java.util.function.Consumer;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Consumer<PacketWriter> consumer;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.write(consumer);
        writer.writeByte((byte) 0xFF);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
