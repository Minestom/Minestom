package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

/**
 * Sent from the client when in spectator mode, and uses the primary click.
 *
 * @param targetId the target entity id clicked, null if not present.
 */
public record ClientSpectatorActionPacket(@Nullable Integer targetId) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientSpectatorActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.OPTIONAL_VAR_INT, ClientSpectatorActionPacket::targetId,
            ClientSpectatorActionPacket::new
    );
}