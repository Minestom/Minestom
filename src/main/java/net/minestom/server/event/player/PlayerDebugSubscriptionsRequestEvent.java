package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.debug.DebugSubscription;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

/**
 * An event wrapper for {@link net.minestom.server.network.packet.client.play.ClientDebugSubscriptionRequestPacket}
 * which is called when any {@link DebugSubscription} is requested/removed/updated by the client
 * with all {@code subscriptions} in its entirety, with entries missing if unregistering from last event,
 * <br>
 * For example by commonly pressing F3-2 for {@link DebugSubscription#DEDICATED_SERVER_TICK_TIME}
 * will be a set containing {@link DebugSubscription#DEDICATED_SERVER_TICK_TIME}
 * and requesting a {@link DebugSubscription#BEES} will be an event where {@code subscriptions} contains both subscriptions.
 * <br>
 * By default, no response ({@link net.minestom.server.network.packet.server.play.DebugEventPacket}) is sent by the server
 * and no response is required if you choose to ignore.
 */
public class PlayerDebugSubscriptionsRequestEvent implements PlayerEvent {
    private final Player player;
    private final Set<DebugSubscription<?>> subscriptions;

    /**
     * Construct a new {@link PlayerDebugSubscriptionsRequestEvent}
     *
     * @param player player
     * @param subscriptions subscriptions
     */
    @ApiStatus.Experimental
    public PlayerDebugSubscriptionsRequestEvent(Player player, Set<DebugSubscription<?>> subscriptions) {
        this.player = Objects.requireNonNull(player, "player");
        this.subscriptions = Objects.requireNonNull(subscriptions, "subscriptions");
    }

    /**
     * Gets the subscriptions requested by the player.
     * <br>
     * To determine which subscriptions were added or removed, compare this set
     * with the previously stored one (using set difference operations)
     *
     * @return the subscriptions
     */
    public @Unmodifiable Set<DebugSubscription<?>> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Checks if there are any subscriptions requested.
     *
     * @return true if {@link #getSubscriptions()} is not empty.
     */
    public boolean wantsSubscriptions() {
        return !subscriptions.isEmpty();
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
