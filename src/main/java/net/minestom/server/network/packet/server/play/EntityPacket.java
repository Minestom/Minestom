package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityPacket implements ServerPacket {

    public int entityId;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_MOVEMENT;
    }
}
