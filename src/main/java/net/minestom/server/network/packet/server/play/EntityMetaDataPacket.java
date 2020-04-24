package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

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
