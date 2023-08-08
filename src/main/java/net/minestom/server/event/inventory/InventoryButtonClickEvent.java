package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player clicks an inventory button.
 * See <a href="https://wiki.vg/Protocol#Click_Container_Button">wiki.vg</a> for slot number details.
 */
public class InventoryButtonClickEvent implements InventoryEvent, PlayerInstanceEvent {

    private final Inventory inventory;
    private final Player player;
    private final byte button;

    public InventoryButtonClickEvent(@NotNull Inventory inventory, @NotNull Player player, byte button) {
        this.inventory = inventory;
        this.player = player;
        this.button = button;
    }

    /**
     * Gets the player who clicked the button in the inventory.
     *
     * @return the player who clicked
     */
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the inventory button number that the player clicked. This is different from inventory slots.
     * @return the button clicked by the player
     */
    public byte getButton() {
        return button;
    }
}
