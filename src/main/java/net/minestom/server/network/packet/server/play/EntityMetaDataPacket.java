package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class EntityMetaDataPacket implements ServerPacket {

    public int entityId;
    public Consumer<BinaryWriter> consumer;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.write(consumer);
        writer.writeByte((byte) 0xFF);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_METADATA;
    }
}
