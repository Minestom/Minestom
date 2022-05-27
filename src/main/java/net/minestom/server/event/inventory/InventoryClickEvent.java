package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.MultipleItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called after {@link InventoryPreClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public class InventoryClickEvent implements MultipleItemEvent, InventoryEvent, PlayerEvent, EntityInstanceEvent {

    private final Inventory inventory;
    private final Player player;
    private final int slot;
    private final ClickType clickType;
    private final ItemStack[] itemStacks;

    public InventoryClickEvent(@Nullable Inventory inventory, @NotNull Player player,
                               int slot, @NotNull ClickType clickType,
                               @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        this.inventory = inventory;
        this.player = player;
        this.slot = slot;
        this.clickType = clickType;
        this.itemStacks = new ItemStack[]{ clicked, cursor };
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
     * Gets the clicked slot number.
     *
     * @return the clicked slot number
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the click type.
     *
     * @return the click type
     */
    @NotNull
    public ClickType getClickType() {
        return clickType;
    }

    /**
     * Gets the clicked item.
     *
     * @return the clicked item
     */
    @NotNull
    public ItemStack getClickedItem() {
        return itemStacks[0];
    }

    /**
     * Gets the item in the player cursor.
     *
     * @return the cursor item
     */
    @NotNull
    public ItemStack getCursorItem() {
        return itemStacks[1];
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }

    @Override
    public @NotNull ItemStack[] getItemStacks() { return itemStacks; }
}
