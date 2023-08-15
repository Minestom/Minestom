package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;

/**
 * Provides standard implementations of most click functions.
 */
public class StandardClickHandler implements ClickHandler {

    /**
     * A generic interface for providing options for clicks like shift clicks and double clicks.
     */
    @FunctionalInterface
    public interface SlotSuggestor {

        /**
         * Suggests slots to be used for this operation.
         * @param player the player clicking
         * @param clickedInventory the clicked inventory
         * @param item the item clicked
         * @param slot the slot of the clicked item
         * @return the list of slots, in order of priority, to be used for this operation
         */
        @NotNull IntIterator get(@NotNull Player player, @NotNull AbstractInventory clickedInventory, @NotNull ItemStack item, int slot);

    }

    private static final @NotNull StackingRule RULE = StackingRule.get();

    private final @NotNull SlotSuggestor shiftClickSlots, doubleClickSlots;

    /**
     * @param shiftClickSlots the shift click slot supplier
     * @param doubleClickSlots the double click slot supplier
     */
    public StandardClickHandler(@NotNull SlotSuggestor shiftClickSlots, @NotNull SlotSuggestor doubleClickSlots) {
        this.shiftClickSlots = shiftClickSlots;
        this.doubleClickSlots = doubleClickSlots;
    }

    private @NotNull ItemStack get(int slot, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        return slot >= ClickPreprocessor.PLAYER_INVENTORY_OFFSET ?
                player.getInventory().getItemStack(slot - ClickPreprocessor.PLAYER_INVENTORY_OFFSET) :
                clickedInventory.getItemStack(slot);
    }

    public @NotNull ClickResult leftClick(@NotNull ClickInfo.LeftClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        ItemStack cursor = clickedInventory.getCursorItem(player);
        ItemStack clickedItem = get(info.clickedSlot(), player, clickedInventory);

        if (cursor.isAir() && clickedItem.isAir()) { // Both are air, no changes
            return ClickResult.empty();
        } else if (RULE.canBeStacked(cursor, clickedItem)) { // Stackable items, combine their counts
            int total = RULE.getAmount(cursor) + RULE.getAmount(clickedItem);
            int maxSize = RULE.getMaxSize(cursor);
            if (RULE.canApply(clickedItem, total)) { // Apply all
                cursor = RULE.apply(cursor, 0);
                clickedItem = RULE.apply(clickedItem, total);
            } else { // Apply all possible
                cursor = RULE.apply(cursor, total - maxSize);
                clickedItem = RULE.apply(clickedItem, maxSize);
            }
            return ClickResult.builder().cursor(cursor).change(info.clickedSlot(), clickedItem).build();
        } else { // Unstackable items, switch them
            return ClickResult.builder().cursor(clickedItem).change(info.clickedSlot(), cursor).build();
        }
    }

    public @NotNull ClickResult rightClick(@NotNull ClickInfo.RightClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        ItemStack cursor = clickedInventory.getCursorItem(player);
        ItemStack clickedItem = get(info.clickedSlot(), player, clickedInventory);

        if (cursor.isAir() && clickedItem.isAir()) { // Both are air, no changes
            return ClickResult.empty();
        } else if (RULE.canBeStacked(clickedItem, cursor)) { // Stackable items, transfer one over
            int newAmount = RULE.getAmount(clickedItem) + 1;
            if (RULE.canApply(clickedItem, newAmount)) { // Transfer one count over
                cursor = RULE.apply(cursor, count -> count - 1);
                clickedItem = RULE.apply(clickedItem, newAmount);
                return ClickResult.builder().cursor(cursor).change(info.clickedSlot(), clickedItem).build();
            } else { // Should have worked, so let's do nothing
                return ClickResult.empty();
            }
        } else {
            if (cursor.isAir()) { // Take half (rounded up) of the clicked item
                int newAmount = (int) Math.ceil(RULE.getAmount(clickedItem) / 2d);
                cursor = RULE.apply(clickedItem, newAmount);
                clickedItem = RULE.apply(clickedItem, count -> count - newAmount);
                return ClickResult.builder().cursor(cursor).change(info.clickedSlot(), clickedItem).build();
            } else if (clickedItem.isAir()) { // Leave one of the cursor on the slot
                clickedItem = RULE.apply(cursor, 1);
                cursor = RULE.apply(cursor, count -> count - 1);
                return ClickResult.builder().cursor(cursor).change(info.clickedSlot(), clickedItem).build();
            } else { // Two existing of items of different types, so switch
                return ClickResult.builder().cursor(clickedItem).change(info.clickedSlot(), cursor).build();
            }
        }
    }

    public @NotNull ClickResult dropSlot(@NotNull ClickInfo.DropSlot info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var item = get(info.clickedSlot(), player, clickedInventory);
        if (item.isAir()) { // Do nothing
            return ClickResult.empty();
        } else if (info.all()) { // Drop everything
            return ClickResult.builder().change(info.clickedSlot(), ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(item)).build();
        } else { // Drop one, and the item must have at least one count
            var droppedItem = RULE.apply(item, 1);
            var newItem = RULE.apply(item, count -> count - 1);
            return ClickResult.builder().change(info.clickedSlot(), newItem).sideEffects(new ClickResult.SideEffects.DropFromPlayer(droppedItem)).build();
        }
    }

    public @NotNull ClickResult dropCursor(@NotNull ClickInfo.DropCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var cursor = clickedInventory.getCursorItem(player);
        if (cursor.isAir()) { // Do nothing
            return ClickResult.empty();
        } else if (info.all()) { // Drop everything
            return ClickResult.builder().cursor(ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(cursor)).build();
        } else { // Drop one, and the item must have at least one count
            var droppedItem = RULE.apply(cursor, 1);
            var newCursor = RULE.apply(cursor, count -> count - 1);
            return ClickResult.builder().cursor(newCursor).sideEffects(new ClickResult.SideEffects.DropFromPlayer(droppedItem)).build();
        }
    }

    public @NotNull ClickResult hotbarSwap(@NotNull ClickInfo.HotbarSwap info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var hotbarSlot = PlayerInventory.HOTBAR_START + info.hotbarSlot();

        var hotbarItem = player.getInventory().getItemStack(hotbarSlot);
        var selectedItem = get(info.clickedSlot(), player, clickedInventory);

        if (hotbarItem.isAir() && selectedItem.isAir()) {
            return ClickResult.empty();
        } else {
            return ClickResult.builder()
                    .change(hotbarSlot, selectedItem, true)
                    .change(info.clickedSlot(), hotbarItem)
                    .build();
        }
    }

    public @NotNull ClickResult offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var offhandSlot = PlayerInventory.OFFHAND_SLOT;

        var offhandItem = player.getInventory().getItemStack(offhandSlot);
        var selectedItem = get(info.clickedSlot(), player, clickedInventory);

        if (offhandItem.isAir() && selectedItem.isAir()) {
            return ClickResult.empty();
        } else {
            return ClickResult.builder()
                    .change(offhandSlot, selectedItem, true)
                    .change(info.clickedSlot(), offhandItem)
                    .build();
        }
    }

    public @NotNull ClickResult copyItem(@NotNull ClickInfo.CopyItem info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var item = get(info.clickedSlot(), player, clickedInventory);
        var cursor = clickedInventory.getCursorItem(player);
        if (cursor.isAir() && !item.isAir()) {
            return ClickResult.builder().cursor(item).build();
        } else {
            return ClickResult.empty();
        }
    }

    public @NotNull ClickResult copyCursor(@NotNull ClickInfo.CopyCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var cursor = clickedInventory.getCursorItem(player);

        ClickResult.Builder builder = ClickResult.builder();
        for (int slot : info.includedSlots()) {
            if (get(slot, player, clickedInventory).isAir()) {
                builder.change(slot, cursor);
            }
        }

        return builder.build();
    }

    public @NotNull ClickResult distributeCursor(@NotNull ClickInfo.DistributeCursor info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var slots = info.includedSlots();
        var cursor = clickedInventory.getCursorItem(player);

        if (cursor.isAir()) return ClickResult.empty();

        final var originalCursorAmount = RULE.getAmount(cursor);
        var cursorAmount = originalCursorAmount;

        ClickResult.Builder builder = ClickResult.builder();

        if (info.evenlyDistribute()) {
            int countPerSlot = (int) Math.floor(cursorAmount / (double) slots.size());
            var iter = slots.iterator();

            while (iter.hasNext() && cursorAmount > 0) {
                var next = iter.nextInt();

                var slotItem = get(next, player, clickedInventory);
                if (slotItem.isAir()) {
                    cursorAmount -= countPerSlot;
                    builder.change(next, RULE.apply(cursor, countPerSlot));
                } else if (RULE.canBeStacked(cursor, slotItem)) {
                    int total = RULE.getAmount(slotItem) + countPerSlot;
                    int maxSize = RULE.getMaxSize(slotItem);

                    if (RULE.canApply(slotItem, total)) { // Add all
                        cursorAmount -= countPerSlot;
                        builder.change(next, RULE.apply(slotItem, total));
                    } else { // Apply all possible
                        var countUsed = maxSize - RULE.getAmount(slotItem);
                        if (countUsed <= 0) continue;

                        cursorAmount -= countUsed;
                        builder.change(next, RULE.apply(slotItem, maxSize));
                    }
                }
            }

        } else {
            var iter = slots.iterator();

            while (iter.hasNext() && cursorAmount > 0) {
                var next = iter.nextInt();

                var slotItem = get(next, player, clickedInventory);
                if (slotItem.isAir()) {
                    cursorAmount--;
                    builder.change(next, RULE.apply(cursor, 1));
                } else if (RULE.canBeStacked(cursor, slotItem)) {
                    var newAmount = RULE.getAmount(slotItem) + 1;
                    if (RULE.canApply(slotItem, newAmount)) {
                        cursorAmount--;
                        builder.change(next, RULE.apply(slotItem, newAmount));
                    }
                }
            }

        }

        if (originalCursorAmount != cursorAmount && RULE.canApply(cursor, cursorAmount)) {
            builder.cursor(RULE.apply(cursor, cursorAmount));
        }

        return builder.build();
    }

    public @NotNull ClickResult shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var slot = info.clickedSlot();
        var clickedItem = get(slot, player, clickedInventory);

        if (clickedItem.isAir()) {
            return ClickResult.empty();
        }

        final var originalItemAmount = RULE.getAmount(clickedItem);
        var itemAmount = originalItemAmount;

        var slots = shiftClickSlots.get(player, clickedInventory, clickedItem, info.clickedSlot());
        ClickResult.Builder builder = ClickResult.builder();

        while (slots.hasNext() && itemAmount > 0) {
            var next = slots.nextInt();
            var slotItem = get(next, player, clickedInventory);

            if (slot == next || !RULE.canBeStacked(clickedItem, slotItem)) continue;

            var maxSize = RULE.getMaxSize(slotItem);
            var slotSize = RULE.getAmount(slotItem);
            if (slotSize >= maxSize) continue;

            var sum = itemAmount + slotSize;

            if (sum <= maxSize) {
                builder.change(next, RULE.apply(slotItem, sum));
                itemAmount = 0;
            } else {
                builder.change(next, RULE.apply(slotItem, maxSize));
                itemAmount = sum - maxSize;
            }
        }

        if (itemAmount > 0) { // Deposit the remaining amount in the first air slot.
            var airSlots = shiftClickSlots.get(player, clickedInventory, clickedItem, info.clickedSlot());

            while (airSlots.hasNext()) {
                var next = airSlots.nextInt();
                var slotItem = get(next, player, clickedInventory);

                if (slot == next || !slotItem.isAir()) continue;

                builder.change(next, RULE.apply(clickedItem, itemAmount));
                itemAmount = 0;

                break;
            }
        }

        // Final remaining item check
        if (originalItemAmount != itemAmount && RULE.canApply(clickedItem, itemAmount)) {
            builder.change(slot, RULE.apply(clickedItem, itemAmount));
        }

        return builder.build();
    }

    public @NotNull ClickResult doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        var cursor = clickedInventory.getCursorItem(player);
        if (cursor.isAir()) {
            return ClickResult.empty();
        }

        final var maxSize = RULE.getMaxSize(cursor);
        final var originalCursorAmount = RULE.getAmount(cursor);
        var cursorAmount = originalCursorAmount;

        var slots = doubleClickSlots.get(player, clickedInventory, cursor, info.clickedSlot());
        ClickResult.Builder builder = ClickResult.builder();

        while (slots.hasNext() && cursorAmount < maxSize) {
            var next = slots.nextInt();

            var slotItem = get(next, player, clickedInventory);
            if (slotItem.isAir() || !RULE.canBeStacked(cursor, slotItem)) continue;

            var sum = cursorAmount + RULE.getAmount(slotItem);

            if (sum <= maxSize) {
                cursorAmount = sum;
                builder.change(next, RULE.apply(slotItem, 0));
            } else {
                cursorAmount = maxSize;
                builder.change(next, RULE.apply(slotItem, sum - maxSize));
            }
        }

        if (originalCursorAmount != cursorAmount && RULE.canApply(cursor, cursorAmount)) {
            builder.cursor(RULE.apply(cursor, cursorAmount));
        }

        return builder.build();
    }

}
