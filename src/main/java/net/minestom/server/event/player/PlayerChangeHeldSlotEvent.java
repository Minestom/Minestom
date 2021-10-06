package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player change his held slot (by pressing 1-9 keys).
 */
public class PlayerChangeHeldSlotEvent implements PlayerEvent, EntityInstanceEvent, CancellableEvent {

    private final Player player;
    private byte slot;

    private boolean cancelled;

    public PlayerChangeHeldSlotEvent(@NotNull Player player, byte slot) {
        this.player = player;
        this.slot = slot;
    }

    /**
     * Gets the slot which the player will held.
     *
     * @return the held slot
     */
    public byte getSlot() {
        return slot;
    }

    /**
     * Changes the final held slot of the player.
     *
     * @param slot the new held slot
     * @throws IllegalArgumentException if <code>slot</code> is not between 0 and 8
     */
    public void setSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "The held slot needs to be between 0 and 8");
        this.slot = slot;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
