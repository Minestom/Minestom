package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.debug.DebugSubscription;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.Set;

public record ClientDebugSubscriptionRequestPacket(
        Set<DebugSubscription<?>> subscriptions
) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientDebugSubscriptionRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            DebugSubscription.NETWORK_TYPE.set(), ClientDebugSubscriptionRequestPacket::subscriptions,
            ClientDebugSubscriptionRequestPacket::new);
}
