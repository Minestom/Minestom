package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.play.SwingAnimationPacket;

/**
 * Sent when the player left clicks. The server picks the swinging hand and its animation, then broadcasts
 * {@link SwingAnimationPacket}.
 */
public record ClientPunchPacket() implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientPunchPacket> SERIALIZER =
            NetworkBufferTemplate.template(new ClientPunchPacket());
}
