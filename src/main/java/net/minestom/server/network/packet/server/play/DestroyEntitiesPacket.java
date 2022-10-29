package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record DestroyEntitiesPacket(@NotNull List<Integer> entityIds) implements ServerPacket {
    public DestroyEntitiesPacket {
        entityIds = List.copyOf(entityIds);
    }

    public DestroyEntitiesPacket(int entityId) {
        this(List.of(entityId));
    }

    public DestroyEntitiesPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(VAR_INT, entityIds);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
