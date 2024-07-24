package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The ClientSpectatePacket is sent when the client interacts with their hot-bar to switch between entities.
 * Contrary to its name, it is actually used to teleport the player to the entity they are switching to,
 * rather than spectating them.
 */
public record ClientSpectatePacket(@NotNull UUID target) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSpectatePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID, ClientSpectatePacket::target,
            ClientSpectatePacket::new);
}
