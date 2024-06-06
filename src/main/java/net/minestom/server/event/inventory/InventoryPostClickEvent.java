package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.ClickUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called after {@link InventoryClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public class InventoryPostClickEvent implements InventoryEvent, PlayerInstanceEvent {

    private final PlayerInventory playerInventory;
    private final Player player;
    private final Inventory inventory;
    private final Click.Info info;
    private final List<Click.Change> changes;

    public InventoryPostClickEvent(@NotNull PlayerInventory playerInventory, @NotNull Player player, @NotNull Inventory inventory,
                                   @NotNull Click.Info info, @NotNull List<Click.Change> changes) {
        this.playerInventory = playerInventory;
        this.player = player;
        this.inventory = inventory;
        this.info = info;
        this.changes = changes;
    }

    /**
     * Gets the player who clicked in the inventory.
     *
     * @return the player who clicked in the inventory
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player inventory that was involved with the click.
     *
     * @return the player inventory
     */
    public @NotNull PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    /**
     * Gets the info about the click that was already processed.
     *
     * @return the click info
     */
    public @NotNull Click.Info getClickInfo() {
        return info;
    }

    /**
     * @return the click type of this click event
     * @see ClickUtils#getType(Click.Info)
     */
    public @NotNull Click.Type getType() {
        return ClickUtils.getType(info);
    }

    /**
     * @return whether or not the slot from {@link #getSlot()} is in the player inventory (as opposed to the clicked
     *         inventory)
     */
    public boolean isInPlayerInventory() {
        int raw = ClickUtils.getSlot(info);
        return inventory instanceof PlayerInventory || (raw != -1 && raw >= inventory.getSize());
    }

    /**
     * @return the slot that was clicked
     * @see ClickUtils#getSlot(Click.Info)
     */
    public int getSlot() {
        return PlayerInventoryUtils.protocolToMinestom(ClickUtils.getSlot(info), inventory.getSize());
    }

    /**
     * Gets the item that was clicked for this event.
     * @see ClickUtils#getItem(Click.Info, Inventory, PlayerInventory)
     */
    public @NotNull ItemStack getClickedItem() {
        return ClickUtils.getItem(info, inventory, playerInventory);
    }

    /**
     * Gets the changes that occurred as a result of this click.
     *
     * @return the changes
     */
    public @NotNull List<Click.Change> getChanges() {
        return changes;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
