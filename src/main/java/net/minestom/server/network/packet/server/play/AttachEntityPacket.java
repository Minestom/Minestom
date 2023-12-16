package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.INT;

public record AttachEntityPacket(int attachedEntityId, int holdingEntityId) implements ServerPacket {
    public AttachEntityPacket(@NotNull Entity attachedEntity, @Nullable Entity holdingEntity) {
        this(attachedEntity.getEntityId(), holdingEntity != null ? holdingEntity.getEntityId() : -1);
    }

    public AttachEntityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, attachedEntityId);
        writer.write(INT, holdingEntityId);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.ATTACH_ENTITY;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
