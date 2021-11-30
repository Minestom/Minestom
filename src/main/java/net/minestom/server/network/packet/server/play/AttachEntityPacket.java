package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AttachEntityPacket(int attachedEntityId, int holdingEntityId) implements ServerPacket {
    public AttachEntityPacket(@NotNull Entity attachedEntity, @Nullable Entity holdingEntity) {
        this(attachedEntity.getEntityId(), holdingEntity != null ? holdingEntity.getEntityId() : -1);
    }

    public AttachEntityPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(attachedEntityId);
        writer.writeInt(holdingEntityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ATTACH_ENTITY;
    }
}
