package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Called when a player's information is updated or removed for a set of recipients.
 * This event can be cancelled to prevent the update from occurring.
 */
public class PlayerInfoUpdateEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private final InfoUpdateType type;
    private final Set<Player> recipients;

    private boolean cancelled;

    public PlayerInfoUpdateEvent(Player player, InfoUpdateType type, Collection<Player> recipients) {
        this.player = player;
        this.type = type;
        this.recipients = new HashSet<>(recipients);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public InfoUpdateType getType() {
        return type;
    }

    public Set<Player> getRecipients() {
        return recipients;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public enum InfoUpdateType {
        REMOVE,
        UPDATE
    }
}
