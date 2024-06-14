package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerOnGroundChangeEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final boolean onGround;
    private final boolean clientSent;

    private boolean cancelled;

    public PlayerOnGroundChangeEvent(Player player, boolean onGround, boolean clientSent) {
        this.player = player;
        this.onGround = onGround;
        this.clientSent = clientSent;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public boolean isOnGround() {
        return onGround;
    }

    /**
     * @return whether the client sent this change to the server or not
     */
    public boolean isClientSent() {
        return clientSent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
