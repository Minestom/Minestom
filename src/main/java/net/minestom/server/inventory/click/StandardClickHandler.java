package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import net.minestom.server.inventory.TransactionOperator;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Provides standard implementations of most click functions.
 */
public class StandardClickHandler implements ClickHandler {

    /**
     * A generic interface for providing options for clicks like shift clicks and double clicks.<br>
     * This addresses the issue of certain click operations only being able to interact with certain slots: for example,
     * shift clicking an item out of an inventory can only put it in the player's inner inventory slots, and will never
     * put the item anywhere else in the inventory or the player's inventory.<br>
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
        @NotNull IntList get(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot);

    }

    private static final @NotNull StackingRule RULE = StackingRule.get();

    private final @NotNull SlotSuggestor shiftClickSlots, doubleClickSlots;

    /**
     * Handles clicks, given a shift click provider and a double click provider.<br>
     * When shift clicks or double clicks need to be handled, the slots provided from the relevant handler will be
     * checked in their given order.<br>
     * For example, double clicking will collect items of the same type as the cursor; the slots provided by the double
     * click slot provider will be checked sequentially and used if they have the same type as 
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

        Pair<ItemStack, ItemStack> pair = TransactionOperator.STACK_LEFT.apply(clickedItem, cursor);
        if (pair != null) { // Stackable items, combine their counts
            builder.change(info.clickedSlot(), pair.left()).cursor(pair.right());
        } else if (!RULE.canBeStacked(cursor, clickedItem)) { // If they're unstackable, switch them
            builder.change(info.clickedSlot(), cursor).cursor(clickedItem);
        }
    }

    public void rightClick(@NotNull ClickInfo.RightClick info, @NotNull ClickResult.Builder builder) {
        int slot = info.clickedSlot();
        ItemStack cursor = builder.getCursorItem();
        ItemStack clickedItem = builder.get(slot);

        if (cursor.isAir() && clickedItem.isAir()) return; // Both are air, no changes

        if (cursor.isAir()) { // Take half (rounded up) of the clicked item
            int newAmount = (int) Math.ceil(RULE.getAmount(clickedItem) / 2d);
            Pair<ItemStack, ItemStack> cursorSlot = TransactionOperator.stackLeftN(newAmount).apply(cursor, clickedItem);
            if (cursorSlot != null) {
                builder.change(slot, cursorSlot.left()).cursor(cursorSlot.right());
            }
        } else if (clickedItem.isAir() || RULE.canBeStacked(clickedItem, cursor)) { // Can add, transfer one over
            Pair<ItemStack, ItemStack> slotCursor = TransactionOperator.stackLeftN(1).apply(clickedItem, cursor);
            if (slotCursor != null) {
                builder.change(slot, slotCursor.left()).cursor(slotCursor.right());
            }
        } else { // Two existing of items of different types, so switch
            builder.cursor(clickedItem).change(slot, cursor);
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
        var hotbarItem = builder.playerInventory().getItemStack(info.hotbarSlot());
        var selectedItem = builder.get(info.clickedSlot());

        if (!hotbarItem.isAir() || !selectedItem.isAir()) {
            builder.change(info.hotbarSlot(), selectedItem, true).change(info.clickedSlot(), hotbarItem);
        }
    }

    public void offhandSwap(@NotNull ClickInfo.OffhandSwap info, @NotNull ClickResult.Builder builder) {
        var offhandItem = builder.playerInventory().getItemStack(PlayerInventoryUtils.OFF_HAND_SLOT);
        var selectedItem = builder.get(info.clickedSlot());

        if (!offhandItem.isAir() || !selectedItem.isAir()) {
            builder.change(PlayerInventoryUtils.OFF_HAND_SLOT, selectedItem, true).change(info.clickedSlot(), offhandItem);
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

    public void distributeCursor(@NotNull ClickInfo.DragClick info, @NotNull ClickResult.Builder builder) {
        var slots = info.includedSlots();
        var cursor = builder.getCursorItem();

        if (cursor.isAir()) return;

        int countPerSlot = info.evenlyDistribute() ?
                (int) Math.floor(RULE.getAmount(cursor) / (double) slots.size()) : 1;

        ItemStack result = TransactionType.general(TransactionOperator.stackLeftN(countPerSlot), slots).process(cursor, builder);

        if (!result.equals(cursor)) {
            builder.cursor(result);
        }
    }

    public void shiftClick(@NotNull ClickInfo.ShiftClick info, @NotNull ClickResult.Builder builder) {
        int slot = info.clickedSlot();
        ItemStack clicked = builder.get(slot);

        IntList slots = shiftClickSlots.get(builder, clicked, slot);
        slots.removeIf(i -> i == slot);

        ItemStack result = TransactionType.add(slots, slots).process(clicked, builder);

        if (!result.equals(clicked)) {
            builder.change(slot, result);
        }
    }

    public void doubleClick(@NotNull ClickInfo.DoubleClick info, @NotNull ClickResult.Builder builder) {
        var cursor = builder.getCursorItem();
        if (cursor.isAir()) return;

        var slots = doubleClickSlots.get(builder, cursor, info.clickedSlot());

        var unstacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT, (first, second) -> RULE.getAmount(first) < RULE.getAmount(first)), slots);
        var stacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT, (first, second) -> RULE.getAmount(first) == RULE.getAmount(first)), slots);
        var result = TransactionType.join(unstacked, stacked).process(cursor, builder);

        if (!result.equals(cursor)) {
            builder.cursor(result);
        }
    }

}
