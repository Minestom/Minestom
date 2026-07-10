package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record CollectItemPacket(int collectedEntityId, int collectorEntityId, int pickupItemCount)
        implements ServerPacket.Play {
    public static final NetworkBuffer.Type<CollectItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, CollectItemPacket::collectedEntityId,
            VAR_INT, CollectItemPacket::collectorEntityId,
            VAR_INT, CollectItemPacket::pickupItemCount,
            CollectItemPacket::new);
}
