package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Called when a player's information is updated or removed for a set of recipients.
 * This event can be cancelled to prevent the update from occurring.
 */
public class PlayerInfoUpdateEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private final InfoUpdateType type;
    private final Collection<Player> originalRecipients;

    private @Nullable Set<Player> recipients; // null until modified

    private boolean cancelled;

    public PlayerInfoUpdateEvent(Player player, InfoUpdateType type, Collection<Player> recipients) {
        this.player = player;
        this.type = type;
        this.originalRecipients = recipients;
        this.recipients = null; // lazy
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public InfoUpdateType getType() {
        return type;
    }

    public Set<Player> getRecipients() {
        if (recipients == null) recipients = new HashSet<>(originalRecipients); // lazy copy

        return recipients;
    }

    /**
     * Returns the effective recipients for this event. This method is for internal use only.
     * @return an unmodifiable collection of players
     */
    @ApiStatus.Internal
    public Collection<Player> getEffectiveRecipients() {
        return Collections.unmodifiableCollection(recipients != null ? recipients : originalRecipients);
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
