package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
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
