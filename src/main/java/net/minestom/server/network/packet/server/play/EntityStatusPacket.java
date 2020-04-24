package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

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
        return ServerPacketIdentifier.ENTITY_STATUS;
    }
}
