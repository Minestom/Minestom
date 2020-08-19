package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityStatusPacket implements ServerPacket {

    public int entityId;
    public byte status;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeInt(entityId);
        writer.writeByte(status);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_STATUS;
    }
}
