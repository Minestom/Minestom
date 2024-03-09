package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPostClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
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
    default @Nullable ClickResult handleClick(@NotNull Inventory inventory, @NotNull Player player, @NotNull ClickInfo clickInfo) {
        InventoryPreClickEvent preClickEvent = new InventoryPreClickEvent(player.getInventory(), inventory, player, clickInfo);
        EventDispatcher.call(preClickEvent);

        ClickInfo newInfo = preClickEvent.getClickInfo();

        if (!preClickEvent.isCancelled()) {
            ClickResult changes = handle(newInfo, ClickResult.builder(player, inventory)).build();

            InventoryClickEvent clickEvent = new InventoryClickEvent(player.getInventory(), inventory, player, newInfo, changes);
            EventDispatcher.call(clickEvent);

            if (!clickEvent.isCancelled()) {
                ClickResult newChanges = clickEvent.getChanges();
                newChanges.applyChanges(player, inventory);

                var postClickEvent = new InventoryPostClickEvent(player, inventory, newInfo, newChanges);
                EventDispatcher.call(postClickEvent);

                if (!clickInfo.equals(newInfo) || !changes.equals(newChanges)) {
                    inventory.update(player);
                    if (inventory != player.getInventory()) {
                        player.getInventory().update(player);
                    }
                }

                return newChanges;
            }
        }

        inventory.update(player);
        if (inventory != player.getInventory()) {
            player.getInventory().update(player);
        }
        return null;
    }

    /**
     * Handles the provided click info that is of any type.
     * @param info the info, of unknown type
     * @param builder the click result builder for this click
     * @return the changes that were calculated
     */
    default @NotNull ClickResult.Builder handle(@NotNull ClickInfo info, @NotNull ClickResult.Builder builder) {
        if (info instanceof ClickInfo.LeftClick left) {
            leftClick(left, builder);
        } else if (info instanceof ClickInfo.RightClick right) {
            rightClick(right, builder);
        } else if (info instanceof ClickInfo.DropSlot drop) {
            dropSlot(drop, builder);
        } else if (info instanceof ClickInfo.DropCursor drop) {
            dropCursor(drop, builder);
        } else if (info instanceof ClickInfo.HotbarSwap swap) {
            hotbarSwap(swap, builder);
        } else if (info instanceof ClickInfo.OffhandSwap swap) {
            offhandSwap(swap, builder);
        } else if (info instanceof ClickInfo.DragClick dragClick) {
            distributeCursor(dragClick, builder);
        } else if (info instanceof ClickInfo.ShiftClick shift) {
            shiftClick(shift, builder);
        } else if (info instanceof ClickInfo.DoubleClick doubleClick) {
            doubleClick(doubleClick, builder);
        } else if (info instanceof ClickInfo.CreativeCopyItem copy) {
            creativeCopyItem(copy, builder);
        } else if (info instanceof ClickInfo.CreativeSetItem set) {
            creativeSetItem(set, builder);
        } else if (info instanceof ClickInfo.CreativeCopyCursor copy) {
            creativeCopyCursor(copy, builder);
        } else if (info instanceof ClickInfo.CreativeDropItem drop) {
            creativeDropItem(drop, builder);
        }

        return builder;
    }

    void leftClick(@NotNull ClickInfo.LeftClick info, @NotNull ClickResult.Builder builder);

    void rightClick(@NotNull ClickInfo.RightClick info, @NotNull ClickResult.Builder builder);

    void dropSlot(@NotNull ClickInfo.DropSlot info, @NotNull ClickResult.Builder builder);

    void dropCursor(@NotNull ClickInfo.DropCursor info, @NotNull ClickResult.Builder builder);

    void hotbarSwap(@NotNull ClickInfo.HotbarSwap info, @NotNull ClickResult.Builder builder);

    void offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull ClickResult.Builder builder);

    void distributeCursor(@NotNull ClickInfo.DragClick info, @NotNull ClickResult.Builder builder);

    void shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull ClickResult.Builder builder);

    void doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull ClickResult.Builder builder);

    void creativeCopyItem(@NotNull ClickInfo.CreativeCopyItem info, @NotNull ClickResult.Builder builder);

    void creativeSetItem(@NotNull ClickInfo.CreativeSetItem info, @NotNull ClickResult.Builder builder);

    void creativeCopyCursor(@NotNull ClickInfo.CreativeCopyCursor info, @NotNull ClickResult.Builder builder);

    void creativeDropItem(@NotNull ClickInfo.CreativeDropItem info, @NotNull ClickResult.Builder builder);

}
