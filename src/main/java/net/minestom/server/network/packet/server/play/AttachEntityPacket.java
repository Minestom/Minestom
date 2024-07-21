package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record AttachEntityPacket(int attachedEntityId, int holdingEntityId) implements ServerPacket.Play {
    public AttachEntityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, attachedEntityId);
        writer.write(INT, holdingEntityId);
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.ATTACH_ENTITY;
    }
}
