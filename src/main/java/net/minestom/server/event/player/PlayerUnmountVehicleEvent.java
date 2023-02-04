package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is finished eating.
 */
public class PlayerUnmountVehicleEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final Entity vehicle;
    private boolean cancelled;

    public PlayerUnmountVehicleEvent(@NotNull Player player, @NotNull Entity vehicle) {
        this.player = player;
        this.vehicle = vehicle;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
    public Entity getVehicle() {
        return vehicle;
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
