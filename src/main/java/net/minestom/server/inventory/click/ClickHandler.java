package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles different types of clicks by players in an inventory.
 * The inventory is provided to this handler in the case of handlers that don't have internal state and may manage
 * multiple inventories, but it's also possible to store the inventory yourself and control usages of it.
 */
public interface ClickHandler {

    /**
     * Handles the provided click from the given player, returning the results after it is applied. If the results are
     * null, this indicates that the click was cancelled or was otherwise not processed.
     *
     * @param inventory the clicked inventory
     * @param player the player that clicked
     * @param clickInfo the information about the player's click
     * @return the results of the click, or null if the click was cancelled or otherwise was not handled
     */
    default @Nullable ClickResult handleClick(@NotNull AbstractInventory inventory, @NotNull Player player, @NotNull ClickInfo clickInfo) {
        // Call a pre-click event with the base click info
        var preClickEvent = new InventoryPreClickEvent(player.getInventory(), inventory, player, clickInfo);
        EventDispatcher.call(preClickEvent);
        clickInfo = preClickEvent.getClickInfo();

        ClickResult changes = null;

        // Apply the click handler if the click will still occur
        if (!preClickEvent.isCancelled()) {
            changes = tryHandle(clickInfo, player, inventory);
        }

        // Apply each of the conditions to the changes, updating it as we go along
        for (var condition : inventory.getInventoryConditions()) {
            changes = condition.accept(player, clickInfo, changes);
        }

        // Apply the changes and send out an event if there are actually any changes
        if (changes != null) {
            changes.applyChanges(player, inventory);

            var clickEvent = new InventoryClickEvent(player, inventory, clickInfo, changes);
            EventDispatcher.call(clickEvent);
        }

        // Make sure to update the inventory if indicated
        if (preClickEvent.shouldUpdate() || changes == null) {
            preClickEvent.getPlayerInventory().update(player);
            preClickEvent.getInventory().update(player);
        }

        return changes;
    }

    /**
     * Handles the provided click info without knowing its type, returning null changes if the type could not be
     * handled.
     * @param info the info, of unknown type
     * @param player the player clicking
     * @param clickedInventory the inventory clicked in
     * @return the changes, or null if the click info has an unknown type
     */
    default @Nullable ClickResult tryHandle(@NotNull ClickInfo info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        if (info instanceof ClickInfo.LeftClick left) {
            return leftClick(left, player, clickedInventory);
        } else if (info instanceof ClickInfo.RightClick right) {
            return rightClick(right, player, clickedInventory);
        } else if (info instanceof ClickInfo.DropSlot drop) {
            return dropSlot(drop, player, clickedInventory);
        } else if (info instanceof ClickInfo.DropCursor drop) {
            return dropCursor(drop, player, clickedInventory);
        } else if (info instanceof ClickInfo.HotbarSwap swap) {
            return hotbarSwap(swap, player, clickedInventory);
        } else if (info instanceof ClickInfo.OffhandSwap swap) {
            return offhandSwap(swap, player, clickedInventory);
        } else if (info instanceof ClickInfo.CopyItem copy) {
            return copyItem(copy, player, clickedInventory);
        } else if (info instanceof ClickInfo.CopyCursor copy) {
            return copyCursor(copy, player, clickedInventory);
        } else if (info instanceof ClickInfo.DistributeCursor distributeCursor) {
            return distributeCursor(distributeCursor, player, clickedInventory);
        } else if (info instanceof ClickInfo.ShiftClick shift) {
            return shiftClick(shift, player, clickedInventory);
        } else if (info instanceof ClickInfo.DoubleClick doubleClick) {
            return doubleClick(doubleClick, player, clickedInventory);
        } else {
            return null;
        }
    }

    @NotNull ClickResult leftClick(@NotNull ClickInfo.LeftClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult rightClick(@NotNull ClickInfo.RightClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult dropSlot(@NotNull ClickInfo.DropSlot info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult dropCursor(@NotNull ClickInfo.DropCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult hotbarSwap(@NotNull ClickInfo.HotbarSwap info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult copyItem(@NotNull ClickInfo.CopyItem info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult copyCursor(@NotNull ClickInfo.CopyCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult distributeCursor(@NotNull ClickInfo.DistributeCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

    @NotNull ClickResult doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory);

}
