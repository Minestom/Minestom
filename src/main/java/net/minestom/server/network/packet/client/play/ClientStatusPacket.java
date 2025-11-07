package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientStatusPacket(Action action) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientStatusPacket> SERIALIZER = NetworkBufferTemplate.template(
            Action.NETWORK_TYPE, ClientStatusPacket::action,
            ClientStatusPacket::new);

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;

        public static final NetworkBuffer.Type<Action> NETWORK_TYPE = NetworkBuffer.Enum(Action.class);
    }
}
