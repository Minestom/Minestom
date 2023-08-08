package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.inventory.TransactionOperator;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * Provides standard implementations of most click functions.
 */
public final class ClickProcessors {

    private static final @NotNull StackingRule RULE = StackingRule.get();

    public static @NotNull Click.Result leftClick(int slot, @NotNull Click.Getter getter) {
        ItemStack cursor = getter.cursor();
        ItemStack clickedItem = getter.get(slot);

        Pair<ItemStack, ItemStack> pair = TransactionOperator.STACK_LEFT.apply(clickedItem, cursor);
        if (pair != null) { // Stackable items, combine their counts
            return getter.setter().set(slot, pair.left()).cursor(pair.right()).build();
        } else if (!RULE.canBeStacked(cursor, clickedItem)) { // If they're unstackable, switch them
            return getter.setter().set(slot, cursor).cursor(clickedItem).build();
        } else {
            return Click.Result.nothing();
        }
    }

    public static @NotNull Click.Result rightClick(int slot, @NotNull Click.Getter getter) {
        ItemStack cursor = getter.cursor();
        ItemStack clickedItem = getter.get(slot);

        if (cursor.isAir() && clickedItem.isAir()) return Click.Result.nothing(); // Both are air, no changes

        if (cursor.isAir()) { // Take half (rounded up) of the clicked item
            int newAmount = (int) Math.ceil(RULE.getAmount(clickedItem) / 2d);
            Pair<ItemStack, ItemStack> cursorSlot = TransactionOperator.stackLeftN(newAmount).apply(cursor, clickedItem);
            return cursorSlot == null ? Click.Result.nothing() :
                    getter.setter().cursor(cursorSlot.left()).set(slot, cursorSlot.right()).build();
        } else if (clickedItem.isAir() || RULE.canBeStacked(clickedItem, cursor)) { // Can add, transfer one over
            Pair<ItemStack, ItemStack> slotCursor = TransactionOperator.stackLeftN(1).apply(clickedItem, cursor);
            return slotCursor == null ? Click.Result.nothing() :
                    getter.setter().set(slot, slotCursor.left()).cursor(slotCursor.right()).build();
        } else { // Two existing of items of different types, so switch
            return getter.setter().cursor(clickedItem).set(slot, cursor).build();
        }
    }

    public static @NotNull Click.Result middleClick(int slot, @NotNull Click.Getter getter) {
        var item = getter.get(slot);
        if (getter.cursor().isAir() && !item.isAir()) {
            return getter.setter().cursor(RULE.apply(item, RULE.getMaxSize(item))).build();
        } else {
            return Click.Result.nothing();
        }
    }

    public static @NotNull Click.Result shiftClick(int slot, @NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        ItemStack clicked = getter.get(slot);

        slots = new ArrayList<>(slots);
        slots.removeIf(i -> i == slot);

        Pair<ItemStack, Int2ObjectMap<ItemStack>> result = TransactionType.add(slots, slots).process(clicked, getter::get);
        Click.Setter setter = getter.setter();
        result.right().forEach(setter::set);

        return !result.left().equals(clicked) ?
                setter.set(slot, result.left()).build() :
                setter.build();
    }

    public static @NotNull Click.Result doubleClick(@NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        var cursor = getter.cursor();
        if (cursor.isAir()) Click.Result.nothing();

        var unstacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT, (left, right) -> RULE.getAmount(left) < RULE.getMaxSize(left)), slots);
        var stacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT, (left, right) -> RULE.getAmount(left) == RULE.getMaxSize(left)), slots);

        Pair<ItemStack, Int2ObjectMap<ItemStack>> result = TransactionType.join(unstacked, stacked).process(cursor, getter::get);
        Click.Setter setter = getter.setter();
        result.right().forEach(setter::set);

        return !result.left().equals(cursor) ?
                setter.cursor(result.left()).build() :
                setter.build();
    }

    public static @NotNull Click.Result dragClick(int countPerSlot, @NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        var cursor = getter.cursor();
        if (cursor.isAir()) return Click.Result.nothing();

        Pair<ItemStack, Int2ObjectMap<ItemStack>> result = TransactionType.general(TransactionOperator.stackLeftN(countPerSlot), slots).process(cursor, getter::get);
        Click.Setter setter = getter.setter();
        result.right().forEach(setter::set);

        return !result.left().equals(cursor) ?
                setter.cursor(result.left()).build() :
                setter.build();
    }

    public static @NotNull Click.Result middleDragClick(@NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        var cursor = getter.cursor();

        Click.Setter setter = getter.setter();

        for (int slot : slots) {
            if (getter.get(slot).isAir()) {
                setter.set(slot, cursor);
            }
        }

        return setter.build();
    }

    public static @NotNull Click.Result dropFromCursor(int amount, @NotNull Click.Getter getter) {
        var cursor = getter.cursor();
        if (cursor.isAir()) Click.Result.nothing(); // Do nothing

        var pair = TransactionOperator.stackLeftN(amount).apply(ItemStack.AIR, cursor);
        if (pair == null) return Click.Result.nothing();

        return getter.setter().cursor(pair.right())
                .sideEffects(new Click.SideEffect.DropFromPlayer(pair.left()))
                .build();
    }

    public static @NotNull Click.Result dropFromSlot(int slot, int amount, @NotNull Click.Getter getter) {
        var item = getter.get(slot);
        if (item.isAir()) return Click.Result.nothing(); // Do nothing

        var pair = TransactionOperator.stackLeftN(amount).apply(ItemStack.AIR, item);
        if (pair == null) return Click.Result.nothing();

        return getter.setter().set(slot, pair.right())
                .sideEffects(new Click.SideEffect.DropFromPlayer(pair.left()))
                .build();
    }

    /**
     * Handles clicks, given a shift click provider and a double click provider.<br>
     * When shift clicks or double clicks need to be handled, the slots provided from the relevant handler will be
     * checked in their given order.<br>
     * For example, double clicking will collect items of the same type as the cursor; the slots provided by the double
     * click slot provider will be checked sequentially and used if they have the same type as
     * @param shiftClickSlots the shift click slot supplier
     * @param doubleClickSlots the double click slot supplier
     */
    public static @NotNull BiFunction<Click.@NotNull Info, Click.@NotNull Getter, Click.@NotNull Result> standard(@NotNull SlotSuggestor shiftClickSlots, @NotNull SlotSuggestor doubleClickSlots) {
        return (info, getter) -> switch (info) {
            case Click.Info.Left(int slot) -> leftClick(slot, getter);
            case Click.Info.Right(int slot) -> rightClick(slot, getter);
            case Click.Info.Middle(int slot) -> middleClick(slot, getter);
            case Click.Info.LeftShift(int slot) -> shiftClick(slot, shiftClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.RightShift(int slot) -> shiftClick(slot, shiftClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.Double(int slot) -> doubleClick(doubleClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.LeftDrag(List<Integer> slots) -> {
                int cursorAmount = RULE.getAmount(getter.cursor());
                int amount = (int) Math.floor(cursorAmount / (double) slots.size());
                yield dragClick(amount, slots, getter);
            }
            case Click.Info.RightDrag(List<Integer> slots) -> dragClick(1, slots, getter);
            case Click.Info.MiddleDrag(List<Integer> slots) -> middleDragClick(slots, getter);
            case Click.Info.DropSlot(int slot, boolean all) -> dropFromSlot(slot, all ? RULE.getAmount(getter.get(slot)) : 1, getter);
            case Click.Info.LeftDropCursor() -> dropFromCursor(getter.cursor().amount(), getter);
            case Click.Info.RightDropCursor() -> dropFromCursor(1, getter);
            case Click.Info.MiddleDropCursor() -> Click.Result.nothing();
            case Click.Info.HotbarSwap(int hotbarSlot, int clickedSlot) -> {
                var hotbarItem = getter.player().apply(hotbarSlot);
                var selectedItem = getter.get(clickedSlot);
                if (hotbarItem.equals(selectedItem)) yield Click.Result.nothing();

                yield getter.setter().setPlayer(hotbarSlot, selectedItem).set(clickedSlot, hotbarItem).build();
            }
            case Click.Info.OffhandSwap(int slot) -> {
                var offhandItem = getter.player().apply(PlayerInventoryUtils.OFF_HAND_SLOT);
                var selectedItem = getter.get(slot);
                if (offhandItem.equals(selectedItem)) yield Click.Result.nothing();

                yield getter.setter().setPlayer(PlayerInventoryUtils.OFF_HAND_SLOT, selectedItem).set(slot, offhandItem).build();
            }
            case Click.Info.CreativeSetItem(int slot, ItemStack item) -> getter.setter().set(slot, item).build();
            case Click.Info.CreativeDropItem(ItemStack item) -> getter.setter().sideEffects(new Click.SideEffect.DropFromPlayer(item)).build();
        };
    }

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
        @NotNull IntStream get(@NotNull Click.Getter builder, @NotNull ItemStack item, int slot);

        default @NotNull List<Integer> getList(@NotNull Click.Getter builder, @NotNull ItemStack item, int slot) {
            return get(builder, item, slot).boxed().toList();
        }
    }

}
