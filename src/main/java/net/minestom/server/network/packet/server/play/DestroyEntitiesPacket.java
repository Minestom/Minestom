package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record DestroyEntitiesPacket(@NotNull List<Integer> entityIds) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<DestroyEntitiesPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT.list(Short.MAX_VALUE), DestroyEntitiesPacket::entityIds,
            DestroyEntitiesPacket::new);

    public DestroyEntitiesPacket {
        entityIds = List.copyOf(entityIds);
    }

    public DestroyEntitiesPacket(int entityId) {
        this(List.of(entityId));
    }
}
