package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;

/**
 * Called when a player change his held slot (by pressing 1-9 keys).
 */
public class PlayerChangeHeldSlotEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final byte oldSlot;
    private byte newSlot;

    private boolean cancelled;

    public PlayerChangeHeldSlotEvent(Player player, byte oldSlot, byte newSlot) {
        this.player = player;
        this.oldSlot = oldSlot;
        this.newSlot = newSlot;
    }

    /**
     * Gets the slot which the player will hold.
     * @deprecated Use {@link #getNewSlot()} instead.
     * @return the future slot
     */
    @Deprecated(forRemoval = true)
    public byte getSlot() {
        return newSlot;
    }

    /**
     * Gets the slot number that the player is currently holding
     *
     * @return The slot index that the player currently is holding
     */
    public byte getOldSlot() {
        return oldSlot;
    }

    /**
     * Gets the slot which the player will hold.
     * @return the future slot
     */
    public byte getNewSlot() {
        return newSlot;
    }

    /**
     * Changes the final held slot of the player.
     *
     * @param slot the new held slot
     * @deprecated Use {@link #setNewSlot(byte)} instead
     * @throws IllegalArgumentException if <code>slot</code> is not between 0 and 8
     */
    @Deprecated(forRemoval = true)
    public void setSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "The held slot needs to be between 0 and 8");
        this.newSlot = slot;
    }

    /**
     * Changes the final held slot of the player.
     *
     * @param slot the new held slot
     * @throws IllegalArgumentException if <code>slot</code> is not between 0 and 8
     */
    public void setNewSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "The held slot needs to be between 0 and 8");
        this.newSlot = slot;
    }

    /**
     * Gets the ItemStack in the player's currently held slot
     * @return The ItemStack in the player's currently held slot
     */
    public ItemStack getItemInOldSlot() {
        return player.getInventory().getItemStack(oldSlot);
    }

    /**
     * Gets the ItemStack in the slot the player will hold
     * @return The ItemStack in the final held slot of the player
     */
    public ItemStack getItemInNewSlot() {
        return player.getInventory().getItemStack(newSlot);
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
    public Player getPlayer() {
        return player;
    }
}
