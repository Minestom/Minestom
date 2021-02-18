package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class AttachEntityPacket implements ServerPacket {

    public int attachedEntityId;
    public int holdingEntityId; // Or -1 to detach

    public AttachEntityPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(attachedEntityId);
        writer.writeInt(holdingEntityId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        attachedEntityId = reader.readInt();
        holdingEntityId = reader.readInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ATTACH_ENTITY;
    }
}
