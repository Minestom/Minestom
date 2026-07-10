package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;

/**
 * Represents an event triggered when a player interacts with a button in an {@link AbstractInventory}, such
 * as the entries in a stonecutter, the buttons in an enchanting table, etc.
 * <br>
 * See the <a href="https://minecraft.wiki/w/Java_Edition_protocol/Inventory">minecraft protocol wiki</a> for a
 * list of all button ids.
 */
public class InventoryButtonClickEvent implements InventoryEvent, PlayerInstanceEvent {
    private final Player player;
    private final AbstractInventory inventory;
    private final int buttonId;

    public InventoryButtonClickEvent(Player player, AbstractInventory inventory, int buttonId) {
        this.player = player;
        this.inventory = inventory;
        this.buttonId = buttonId;
    }

    @Override
    public AbstractInventory getInventory() {
        return inventory;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public int getButtonId() {
        return buttonId;
    }
}
