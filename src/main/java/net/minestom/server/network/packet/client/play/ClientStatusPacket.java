package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

public record ClientStatusPacket(@NotNull Action action) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientStatusPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(Action.class), ClientStatusPacket::action,
            ClientStatusPacket::new);

    public enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS
    }
}
