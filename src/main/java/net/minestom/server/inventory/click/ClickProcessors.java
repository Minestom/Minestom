package net.minestom.server.inventory.click;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.TransactionOperator;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.DropFromPlayer;
import net.minestom.server.inventory.click.Click.Change.Container;
import net.minestom.server.inventory.click.Click.Change.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

/**
 * Provides standard implementations of most click functions.
 */
public final class ClickProcessors {
    private static final @NotNull StackingRule RULE = StackingRule.get();

    public static @NotNull List<Click.Change> leftClick(int slot, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        final ItemStack clickedItem = getter.get(slot);

        final TransactionOperator.Entry pair = TransactionOperator.STACK_LEFT.apply(clickedItem, cursor);
        if (pair != null) { // Stackable items, combine their counts
            return List.of(new Container(slot, pair.left()), new Cursor(pair.right()));
        } else if (!RULE.canBeStacked(cursor, clickedItem)) { // If they're unstackable, switch them
            return List.of(new Container(slot, cursor), new Cursor(clickedItem));
        } else {
            return List.of();
        }
    }

    public static @NotNull List<Click.Change> rightClick(int slot, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        final ItemStack clickedItem = getter.get(slot);
        if (cursor.isAir() && clickedItem.isAir()) return List.of(); // Both are air, no changes

        if (cursor.isAir()) { // Take half (rounded up) of the clicked item
            int newAmount = (int) Math.ceil(RULE.getAmount(clickedItem) / 2d);
            final TransactionOperator.Entry cursorSlot = TransactionOperator.stackLeftN(newAmount).apply(cursor, clickedItem);
            if (cursorSlot == null) return List.of();
            return List.of(new Container(slot, cursorSlot.right()), new Cursor(cursorSlot.left()));
        } else if (clickedItem.isAir() || RULE.canBeStacked(clickedItem, cursor)) { // Can add, transfer one over
            final TransactionOperator.Entry slotCursor = TransactionOperator.stackLeftN(1).apply(clickedItem, cursor);
            if (slotCursor == null) return List.of();
            return List.of(new Container(slot, slotCursor.left()), new Cursor(slotCursor.right()));
        } else { // Two existing of items of different types, so switch
            return List.of(new Cursor(clickedItem), new Container(slot, cursor));
        }
    }

    public static @NotNull List<Click.Change> middleClick(int slot, @NotNull Click.Getter getter) {
        final ItemStack item = getter.get(slot);
        if (!getter.cursor().isAir() || item.isAir()) return List.of();
        return List.of(new Cursor(RULE.apply(item, RULE.getMaxSize(item))));
    }

    public static @NotNull List<Click.Change> shiftClick(int slot, @NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        final ItemStack clicked = getter.get(slot);

        slots = new ArrayList<>(slots);
        slots.removeIf(i -> i == slot);

        final TransactionType.Entry result = TransactionType.add(slots, slots).apply(clicked, getter::get);
        List<Click.Change> changes = new ArrayList<>();
        result.changes().forEach((slotId, item) -> changes.add(new Container(slotId, item)));

        if (!result.remaining().equals(clicked)) {
            changes.add(new Container(slot, result.remaining()));
        }

        return changes;
    }

    public static @NotNull List<Click.Change> doubleClick(@NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        if (cursor.isAir()) return List.of();

        final TransactionType unstacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT,
                (left, right) -> RULE.getAmount(left) < RULE.getMaxSize(left)), slots);
        final TransactionType stacked = TransactionType.general(TransactionOperator.filter(TransactionOperator.STACK_RIGHT,
                (left, right) -> RULE.getAmount(left) == RULE.getMaxSize(left)), slots);

        final TransactionType.Entry result = TransactionType.join(unstacked, stacked).apply(cursor, getter::get);
        List<Click.Change> changes = new ArrayList<>();
        result.changes().forEach((slotId, item) -> changes.add(new Container(slotId, item)));

        if (!result.remaining().equals(cursor)) {
            changes.add(new Cursor(result.remaining()));
        }

        return changes;
    }

    public static @NotNull List<Click.Change> dragClick(int countPerSlot, @NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        if (cursor.isAir()) return List.of();

        final TransactionType.Entry result = TransactionType.general(TransactionOperator.stackLeftN(countPerSlot), slots).apply(cursor, getter::get);
        List<Click.Change> changes = new ArrayList<>();
        result.changes().forEach((slotId, item) -> changes.add(new Container(slotId, item)));

        if (!result.remaining().equals(cursor)) {
            changes.add(new Cursor(result.remaining()));
        }

        return changes;
    }

    public static @NotNull List<Click.Change> middleDragClick(@NotNull List<Integer> slots, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        return slots.stream()
                .filter(slot -> getter.get(slot).isAir())
                .map(slot -> (Click.Change) new Container(slot, cursor))
                .toList();
    }

    public static @NotNull List<Click.Change> dropFromCursor(int amount, @NotNull Click.Getter getter) {
        final ItemStack cursor = getter.cursor();
        if (cursor.isAir()) return List.of(); // Do nothing

        final TransactionOperator.Entry pair = TransactionOperator.stackLeftN(amount).apply(ItemStack.AIR, cursor);
        if (pair == null) return List.of();

        return List.of(new Cursor(pair.right()), new DropFromPlayer(pair.left()));
    }

    public static @NotNull List<Click.Change> dropFromSlot(int slot, int amount, @NotNull Click.Getter getter) {
        final ItemStack item = getter.get(slot);
        if (item.isAir()) return List.of(); // Do nothing

        final TransactionOperator.Entry pair = TransactionOperator.stackLeftN(amount).apply(ItemStack.AIR, item);
        if (pair == null) return List.of();

        return List.of(new Container(slot, pair.right()), new DropFromPlayer(pair.left()));
    }

    /**
     * Handles clicks, given a shift click provider and a double click provider.<br>
     * When shift clicks or double clicks need to be handled, the slots provided from the relevant handler will be
     * checked in their given order.<br>
     * For example, double-clicking will collect items of the same type as the cursor; the slots provided by the double
     * click slot provider will be checked sequentially and used if they have the same type as
     *
     * @param shiftClickSlots  the shift click slot supplier
     * @param doubleClickSlots the double click slot supplier
     */
    public static ClickProcessors.@NotNull InventoryProcessor standard(@NotNull SlotSuggestor shiftClickSlots, @NotNull SlotSuggestor doubleClickSlots) {
        return (info, getter) -> switch (info) {
            case Click.Info.Left(int slot) -> leftClick(slot, getter);
            case Click.Info.Right(int slot) -> rightClick(slot, getter);
            case Click.Info.Middle(int slot) -> middleClick(slot, getter);
            case Click.Info.LeftShift(int slot) ->
                    shiftClick(slot, shiftClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.RightShift(int slot) ->
                    shiftClick(slot, shiftClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.Double(int slot) ->
                    doubleClick(doubleClickSlots.getList(getter, getter.get(slot), slot), getter);
            case Click.Info.LeftDrag(List<Integer> slots) -> {
                int cursorAmount = RULE.getAmount(getter.cursor());
                int amount = (int) Math.floor(cursorAmount / (double) slots.size());
                yield dragClick(amount, slots, getter);
            }
            case Click.Info.RightDrag(List<Integer> slots) -> dragClick(1, slots, getter);
            case Click.Info.MiddleDrag(List<Integer> slots) -> middleDragClick(slots, getter);
            case Click.Info.DropSlot(int slot, boolean all) ->
                    dropFromSlot(slot, all ? RULE.getAmount(getter.get(slot)) : 1, getter);
            case Click.Info.LeftDropCursor() -> dropFromCursor(getter.cursor().amount(), getter);
            case Click.Info.RightDropCursor() -> dropFromCursor(1, getter);
            case Click.Info.MiddleDropCursor() -> List.of();
            case Click.Info.HotbarSwap(int hotbarSlot, int clickedSlot) -> {
                var hotbarItem = getter.player().apply(hotbarSlot);
                var selectedItem = getter.get(clickedSlot);
                if (hotbarItem.equals(selectedItem)) yield List.of();

                yield List.of(new Container(clickedSlot, hotbarItem), new Player(hotbarSlot, selectedItem));
            }
            case Click.Info.OffhandSwap(int slot) -> {
                var offhandItem = getter.player().apply(PlayerInventoryUtils.OFF_HAND_SLOT);
                var selectedItem = getter.get(slot);
                if (offhandItem.equals(selectedItem)) yield List.of();

                yield List.of(new Container(slot, offhandItem), new Player(OFF_HAND_SLOT, selectedItem));
            }
            case Click.Info.CreativeSetItem(int slot, ItemStack item) -> List.of(new Container(slot, item));
            case Click.Info.CreativeDropItem(ItemStack item) -> List.of(new DropFromPlayer(item));
        };
    }

    public interface InventoryProcessor extends BiFunction<Click.Info, Click.Getter, List<Click.Change>> {
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
         *
         * @param builder the result builder
         * @param item    the item clicked
         * @param slot    the slot of the clicked item
         * @return the list of slots, in order of priority, to be used for this operation
         */
        @NotNull
        IntStream get(@NotNull Click.Getter builder, @NotNull ItemStack item, int slot);

        default @NotNull List<Integer> getList(@NotNull Click.Getter builder, @NotNull ItemStack item, int slot) {
            return get(builder, item, slot).boxed().toList();
        }
    }

    /**
     * Handle player inventory (without any container open).
     */
    public static final InventoryProcessor PLAYER_PROCESSOR = ClickProcessors.standard(
            (getter, item, slot) -> {
                List<Integer> slots = new ArrayList<>();

                final EquipmentSlot equipmentSlot = item.material().registry().equipmentSlot();
                if (equipmentSlot != null && slot != equipmentSlot.armorSlot()) {
                    slots.add(equipmentSlot.armorSlot());
                }

                if (item.material() == Material.SHIELD && slot != OFF_HAND_SLOT) {
                    slots.add(OFF_HAND_SLOT);
                }

                if (slot < 9 || slot > 35) IntStream.range(9, 36).forEach(slots::add);
                if (slot < 0 || slot > 8) IntStream.range(0, 9).forEach(slots::add);

                if (slot == CRAFT_RESULT) {
                    Collections.reverse(slots);
                }

                return slots.stream().mapToInt(i -> i);
            },
            (getter, item, slot) -> Stream.of(
                    IntStream.range(CRAFT_SLOT_1, CRAFT_SLOT_4 + 1), // 1-4
                    IntStream.range(HELMET_SLOT, BOOTS_SLOT + 1), // 5-8
                    IntStream.range(9, 36), // 9-35
                    IntStream.range(0, 9), // 36-44
                    IntStream.of(OFF_HAND_SLOT) // 45
            ).flatMapToInt(i -> i)
    );

    /**
     * Assumes all the container's slots to be accessible.
     */
    public static final InventoryProcessor GENERIC_PROCESSOR = ClickProcessors.standard(
            (builder, item, slot) -> {
                final int size = builder.mainSize();
                return slot >= size ?
                        IntStream.range(0, size) :
                        PlayerInventoryUtils.getInnerShiftClickSlots(size);
            },
            (builder, item, slot) -> {
                final int size = builder.mainSize();
                return IntStream.concat(
                        IntStream.range(0, size),
                        PlayerInventoryUtils.getInnerDoubleClickSlots(size)
                );
            });


    // SPECIALIZED PROCESSORS DEFINITIONS

    /**
     * Client prediction appears to disallow shift clicking into furnace inventories.<br>
     * Instead:
     * - Shift clicks in the inventory go to the player inventory like normal
     * - Shift clicks in the hotbar go to the storage
     * - Shift clicks in the storage go to the hotbar
     */
    public static final InventoryProcessor FURNACE_PROCESSOR = ClickProcessors.standard(
            (builder, item, slot) -> {
                final int size = builder.mainSize();
                if (slot < size) {
                    return PlayerInventoryUtils.getInnerShiftClickSlots(size);
                } else if (slot < size + 27) {
                    return IntStream.range(27, 36).map(i -> i + size);
                } else {
                    return IntStream.range(0, 27).map(i -> i + size);
                }
            },
            (builder, item, slot) -> {
                final int size = builder.mainSize();
                return IntStream.concat(
                        IntStream.range(0, size),
                        PlayerInventoryUtils.getInnerDoubleClickSlots(size)
                );
            });

    public static final Map<InventoryType, InventoryProcessor> PROCESSORS_MAP = Map.ofEntries(
            entry(InventoryType.FURNACE, FURNACE_PROCESSOR)
    );
}
