package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.InventoryEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called after {@link InventoryPreClickEvent}, this event cannot be cancelled and items related to the click
 * are already moved.
 */
public class InventoryClickEvent extends InventoryEvent {

    private final Player player;
    private final int slot;
    private final ClickType clickType;
    private final ItemStack clickedItem;
    private final ItemStack cursorItem;

    public InventoryClickEvent(@Nullable Inventory inventory, @NotNull Player player,
                               int slot, @NotNull ClickType clickType,
                               @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        super(inventory);
        this.player = player;
        this.slot = slot;
        this.clickType = clickType;
        this.clickedItem = clicked;
        this.cursorItem = cursor;
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
        return clickedItem;
    }

    /**
     * Gets the item in the player cursor.
     *
     * @return the cursor item
     */
    @NotNull
    public ItemStack getCursorItem() {
        return cursorItem;
    }
}
