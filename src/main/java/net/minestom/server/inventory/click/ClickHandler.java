package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
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
        // Call a pre-click event with the base click info
        var preClickEvent = new InventoryPreClickEvent(player.getInventory(), inventory, player, clickInfo);
        EventDispatcher.call(preClickEvent);
        clickInfo = preClickEvent.getClickInfo();

        ClickResult changes = null;

        // Apply the click handler if the click will still occur
        if (!preClickEvent.isCancelled()) {
            var builder = tryHandle(clickInfo, ClickResult.builder(player, inventory));
            if (builder != null) {
                changes = builder.build();
            }
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
            preClickEvent.getEventInventory().update(player);
        }

        return changes;
    }

    /**
     * Handles the provided click info without knowing its type, returning null changes if the type could not be
     * handled.
     * @param info the info, of unknown type
     * @param builder the click result builder for this click
     * @return the changes, or null if the click info has an unknown type
     */
    default @Nullable ClickResult.Builder tryHandle(@NotNull ClickInfo info, @NotNull ClickResult.Builder builder) {
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
        } else if (info instanceof ClickInfo.CopyItem copy) {
            copyItem(copy, builder);
        } else if (info instanceof ClickInfo.CopyCursor copy) {
            copyCursor(copy, builder);
        } else if (info instanceof ClickInfo.DistributeCursor distributeCursor) {
            distributeCursor(distributeCursor, builder);
        } else if (info instanceof ClickInfo.ShiftClick shift) {
            shiftClick(shift, builder);
        } else if (info instanceof ClickInfo.DoubleClick doubleClick) {
            doubleClick(doubleClick, builder);
        } else {
            return null;
        }

        return builder;
    }

    void leftClick(@NotNull ClickInfo.LeftClick info, @NotNull ClickResult.Builder builder);

    void rightClick(@NotNull ClickInfo.RightClick info, @NotNull ClickResult.Builder builder);

    void dropSlot(@NotNull ClickInfo.DropSlot info, @NotNull ClickResult.Builder builder);

    void dropCursor(@NotNull ClickInfo.DropCursor info, @NotNull ClickResult.Builder builder);

    void hotbarSwap(@NotNull ClickInfo.HotbarSwap info, @NotNull ClickResult.Builder builder);

    void offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull ClickResult.Builder builder);

    void copyItem(@NotNull ClickInfo.CopyItem info, @NotNull ClickResult.Builder builder);

    void copyCursor(@NotNull ClickInfo.CopyCursor info, @NotNull ClickResult.Builder builder);

    void distributeCursor(@NotNull ClickInfo.DistributeCursor info, @NotNull ClickResult.Builder builder);

    void shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull ClickResult.Builder builder);

    void doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull ClickResult.Builder builder);

}
