package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class EntityPacket implements ServerPacket {

    public int entityId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_MOVEMENT;
    }
}
