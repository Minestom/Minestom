package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.component.SwingAnimation;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Plays a hand swing on an entity for the receiving client.
 *
 * @param entityId  the id of the swinging entity
 * @param hand      the hand that swings
 * @param animation the animation to play and how many ticks it lasts
 */
public record SwingAnimationPacket(int entityId, PlayerHand hand, SwingAnimation animation) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SwingAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SwingAnimationPacket::entityId,
            PlayerHand.NETWORK_TYPE, SwingAnimationPacket::hand,
            SwingAnimation.NETWORK_TYPE, SwingAnimationPacket::animation,
            SwingAnimationPacket::new);
}
