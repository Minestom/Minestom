package net.minestom.server.event.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLeaveBedEvent implements CancellableEvent, PlayerInstanceEvent {

    private final Player player;
    private boolean cancelled = false;

    public PlayerLeaveBedEvent(Player player) {
        this.player = player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

}
