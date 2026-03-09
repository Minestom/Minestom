package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.Objects;
import java.util.UUID;

public record ClientTeleportToEntityPacket(UUID target) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientTeleportToEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID, ClientTeleportToEntityPacket::target,
            ClientTeleportToEntityPacket::new
    );

    public ClientTeleportToEntityPacket {
        Objects.requireNonNull(target, "target");
    }
}
