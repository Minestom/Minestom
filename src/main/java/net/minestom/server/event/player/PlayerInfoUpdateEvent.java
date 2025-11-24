package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.Set;

public class PlayerInfoUpdateEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private final InfoUpdateType type;
    private final Set<Player> recipients;

    private boolean cancelled;

    public PlayerInfoUpdateEvent(Player player, InfoUpdateType type, Set<Player> recipients) {
        this.player = player;
        this.type = type;
        this.recipients = recipients;
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
