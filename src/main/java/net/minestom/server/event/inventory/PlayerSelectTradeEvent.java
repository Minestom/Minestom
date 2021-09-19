package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} selects a {@link net.minestom.server.network.packet.server.play.TradeListPacket.Trade}
 * in a merchant inventory.
 */
public class PlayerSelectTradeEvent implements InventoryEvent, PlayerEvent {

    private final @NotNull Player player;
    private final @NotNull Inventory inventory;
    private final int slot;

    public PlayerSelectTradeEvent(@NotNull Player player, @NotNull Inventory inventory, int slot) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
    }

    /**
     * Gets the trade slot that was selected
     *
     * @return the trade slot that was selected
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the inventory this happened in.
     *
     * @return the inventory this happened in.
     */
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the player that selected the trade slot.
     *
     * @return the player that selected the trade slot.
     */
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
