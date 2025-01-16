package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} interacts (right-click) with an {@link Entity}.
 */
public record PlayerEntityInteractEvent(@NotNull Player player, @NotNull Entity target, @NotNull PlayerHand hand,
                                        @NotNull Point interactPosition) implements PlayerInstanceEvent {

    /**
     * Gets the {@link Entity} with who {@link #player()} is interacting.
     *
     * @return the {@link Entity}
     */
    @Override
    public @NotNull Entity target() {
        return target;
    }

    /**
     * Gets with which hand the player interacted with the entity.
     *
     * @return the hand
     */
    @Override
    public @NotNull PlayerHand hand() {
        return hand;
    }

    /**
     * Gets the position at which the entity was interacted
     *
     * @see net.minestom.server.network.packet.client.play.ClientInteractEntityPacket.InteractAt
     * @return the interaction position
     */
    @Override
    public @NotNull Point interactPosition() {
        return interactPosition;
    }
}