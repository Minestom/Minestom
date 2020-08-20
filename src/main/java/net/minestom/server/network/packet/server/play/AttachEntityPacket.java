package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class AttachEntityPacket implements ServerPacket {

    public int attachedEntityId;
    public int holdingEntityId; // Or -1 to detach

    @Override
    public void write(BinaryWriter writer) {
        writer.writeInt(attachedEntityId);
        writer.writeInt(holdingEntityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ATTACH_ENTITY;
    }
}
