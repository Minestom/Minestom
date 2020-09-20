package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;

/**
 * Called when a player change his held slot (by pressing 1-9 keys)
 */
public class PlayerChangeHeldSlotEvent extends CancellableEvent {

    private final Player player;
    private byte slot;

    public PlayerChangeHeldSlotEvent(Player player, byte slot) {
        this.player = player;
        this.slot = slot;
    }

    /**
     * Get the player who changed his held slot
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the slot which the player will held
     *
     * @return the held slot
     */
    public byte getSlot() {
        return slot;
    }

    /**
     * Change the final held slot of the player
     *
     * @param slot the new held slot
     * @throws IllegalArgumentException if <code>slot</code> is not between 0 and 8
     */
    public void setSlot(byte slot) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, 8), "The held slot needs to be between 0 and 8");
        this.slot = slot;
    }
}
