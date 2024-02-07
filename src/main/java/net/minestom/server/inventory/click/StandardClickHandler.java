package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntIterator;
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
         * @param builder the result builder
         * @param item the item clicked
         * @param slot the slot of the clicked item
         * @return the list of slots, in order of priority, to be used for this operation
         */
        @NotNull IntIterator get(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot);

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

    public void leftClick(@NotNull ClickInfo.LeftClick info, @NotNull ClickResult.Builder builder) {
        ItemStack cursor = builder.getCursorItem();
        ItemStack clickedItem = builder.get(info.clickedSlot());

        if (cursor.isAir() && clickedItem.isAir()) return; // Both are air, no changes

        if (RULE.canBeStacked(cursor, clickedItem)) { // Stackable items, combine their counts
            int total = RULE.getAmount(cursor) + RULE.getAmount(clickedItem);
            int maxSize = RULE.getMaxSize(cursor);
            if (RULE.canApply(clickedItem, total)) { // Apply all
                cursor = RULE.apply(cursor, 0);
                clickedItem = RULE.apply(clickedItem, total);
            } else { // Apply all possible
                cursor = RULE.apply(cursor, total - maxSize);
                clickedItem = RULE.apply(clickedItem, maxSize);
            }
            builder.cursor(cursor).change(info.clickedSlot(), clickedItem);
        } else { // Unstackable items, switch them
            builder.cursor(clickedItem).change(info.clickedSlot(), cursor);
        }
    }

    public void rightClick(@NotNull ClickInfo.RightClick info, @NotNull ClickResult.Builder builder) {
        ItemStack cursor = builder.getCursorItem();
        ItemStack clickedItem = builder.get(info.clickedSlot());

        if (cursor.isAir() && clickedItem.isAir()) return; // Both are air, no changes

        if (RULE.canBeStacked(clickedItem, cursor)) { // Stackable items, transfer one over
            int newAmount = RULE.getAmount(clickedItem) + 1;
            if (RULE.canApply(clickedItem, newAmount)) { // Transfer one count over if possible. Otherwise, do nothing
                cursor = RULE.apply(cursor, count -> count - 1);
                clickedItem = RULE.apply(clickedItem, newAmount);
                builder.cursor(cursor).change(info.clickedSlot(), clickedItem);
            }
        } else {
            if (cursor.isAir()) { // Take half (rounded up) of the clicked item
                int newAmount = (int) Math.ceil(RULE.getAmount(clickedItem) / 2d);
                cursor = RULE.apply(clickedItem, newAmount);
                clickedItem = RULE.apply(clickedItem, count -> count - newAmount);
                builder.cursor(cursor).change(info.clickedSlot(), clickedItem);
            } else if (clickedItem.isAir()) { // Leave one of the cursor on the slot
                clickedItem = RULE.apply(cursor, 1);
                cursor = RULE.apply(cursor, count -> count - 1);
                builder.cursor(cursor).change(info.clickedSlot(), clickedItem);
            } else { // Two existing of items of different types, so switch
                builder.cursor(clickedItem).change(info.clickedSlot(), cursor);
            }
        }
    }

    public void dropSlot(@NotNull ClickInfo.DropSlot info, @NotNull ClickResult.Builder builder) {
        var item = builder.get(info.clickedSlot());
        if (item.isAir()) return; // Do nothing

        if (info.all()) { // Drop everything
            builder.change(info.clickedSlot(), ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(item));
        } else { // Drop one, and the item must have at least one count
            var droppedItem = RULE.apply(item, 1);
            var newItem = RULE.apply(item, count -> count - 1);
            builder.change(info.clickedSlot(), newItem).sideEffects(new ClickResult.SideEffects.DropFromPlayer(droppedItem));
        }
    }

    public void dropCursor(@NotNull ClickInfo.DropCursor info, @NotNull ClickResult.Builder builder) {
        var cursor = builder.getCursorItem();
        if (cursor.isAir()) return; // Do nothing

        if (info.all()) { // Drop everything
            builder.cursor(ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(cursor));
        } else { // Drop one, and the item must have at least one count
            var droppedItem = RULE.apply(cursor, 1);
            var newCursor = RULE.apply(cursor, count -> count - 1);
            builder.cursor(newCursor).sideEffects(new ClickResult.SideEffects.DropFromPlayer(droppedItem));
        }
    }

    public void hotbarSwap(@NotNull ClickInfo.HotbarSwap info, @NotNull ClickResult.Builder builder) {
        var hotbarSlot = PlayerInventory.HOTBAR_START + info.hotbarSlot();

        var hotbarItem = builder.playerInventory().getItemStack(hotbarSlot);
        var selectedItem = builder.get(info.clickedSlot());

        if (!hotbarItem.isAir() || !selectedItem.isAir()) {
            builder.change(hotbarSlot, selectedItem, true).change(info.clickedSlot(), hotbarItem);
        }
    }

    public void offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull ClickResult.Builder builder) {
        var offhandSlot = PlayerInventory.OFF_HAND_SLOT;

        var offhandItem = builder.playerInventory().getItemStack(offhandSlot);
        var selectedItem = builder.get(info.clickedSlot());

        if (!offhandItem.isAir() || !selectedItem.isAir()) {
            builder.change(offhandSlot, selectedItem, true).change(info.clickedSlot(), offhandItem);
        }
    }

    public void copyItem(@NotNull ClickInfo.CopyItem info, @NotNull ClickResult.Builder builder) {
        var item = builder.get(info.clickedSlot());
        if (builder.getCursorItem().isAir() && !item.isAir()) {
            builder.cursor(item);
        }
    }

    public void copyCursor(@NotNull ClickInfo.CopyCursor info, @NotNull ClickResult.Builder builder) {
        var cursor = builder.getCursorItem();

        for (int slot : info.includedSlots()) {
            if (builder.get(slot).isAir()) {
                builder.change(slot, cursor);
            }
        }
    }

    public void distributeCursor(@NotNull ClickInfo.DistributeCursor info, @NotNull ClickResult.Builder builder) {
        var slots = info.includedSlots();
        var cursor = builder.getCursorItem();

        if (cursor.isAir()) return;

        final var originalCursorAmount = RULE.getAmount(cursor);
        var cursorAmount = originalCursorAmount;

        if (info.evenlyDistribute()) {
            int countPerSlot = (int) Math.floor(cursorAmount / (double) slots.size());
            var iter = slots.iterator();

            while (iter.hasNext() && cursorAmount > 0) {
                var next = iter.nextInt();

                var slotItem = builder.get(next);
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

                var slotItem = builder.get(next);
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
    }

    public void shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull ClickResult.Builder builder) {
        var slot = info.clickedSlot();
        var clickedItem = builder.get(slot);

        if (clickedItem.isAir()) return;

        final var originalItemAmount = RULE.getAmount(clickedItem);
        var itemAmount = originalItemAmount;

        var slots = shiftClickSlots.get(builder, clickedItem, info.clickedSlot());

        while (slots.hasNext() && itemAmount > 0) {
            var next = slots.nextInt();
            var slotItem = builder.get(next);

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
            var airSlots = shiftClickSlots.get(builder, clickedItem, info.clickedSlot());

            while (airSlots.hasNext()) {
                var next = airSlots.nextInt();
                var slotItem = builder.get(next);

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
    }

    public void doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull ClickResult.Builder builder) {
        var cursor = builder.getCursorItem();
        if (cursor.isAir()) return;

        final var maxSize = RULE.getMaxSize(cursor);
        final var originalCursorAmount = RULE.getAmount(cursor);
        var cursorAmount = originalCursorAmount;

        var slots = doubleClickSlots.get(builder, cursor, info.clickedSlot());

        while (slots.hasNext() && cursorAmount < maxSize) {
            var next = slots.nextInt();

            var slotItem = builder.get(next);
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
    }

}
