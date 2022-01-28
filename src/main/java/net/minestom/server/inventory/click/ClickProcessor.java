package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class ClickProcessor {

    public static ClickResult.Single left(int slot, ItemStack clicked, ItemStack cursor) {
        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (cursorRule.canBeStacked(cursor, clicked)) {
            // Try to stack items
            final int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);
            final int maxSize = cursorRule.getMaxSize(cursor);
            if (!clickedRule.canApply(clicked, totalAmount)) {
                // Size is too big, stack as much as possible into clicked
                cursor = cursorRule.apply(cursor, totalAmount - maxSize);
                clicked = clickedRule.apply(clicked, maxSize);
            } else {
                // Merge cursor item clicked
                cursor = cursorRule.apply(cursor, 0);
                clicked = clickedRule.apply(clicked, totalAmount);
            }
        } else {
            // Items are not compatible, swap them
            var temp = cursor;
            cursor = clicked;
            clicked = temp;
        }
        if (cursor.isAir() && clicked.isAir()) {
            // return empty
            return ClickResultImpl.Single.empty();
        }
        return new ClickResultImpl.Single(cursor, Map.of(slot, clicked));
    }

    public static ClickResult.Single right(int slot, ItemStack clicked, ItemStack cursor) {
        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (clickedRule.canBeStacked(clicked, cursor)) {
            // Items can be stacked
            final int amount = clickedRule.getAmount(clicked) + 1;
            if (!clickedRule.canApply(clicked, amount)) {
                // Size too large, stop here
                return new ClickResultImpl.Single(cursor, Map.of());
            } else {
                // Add 1 to clicked
                cursor = cursorRule.apply(cursor, operand -> operand - 1);
                clicked = clickedRule.apply(clicked, amount);
            }
        } else {
            // Items cannot be stacked
            if (cursor.isAir()) {
                // Take half of clicked
                final int amount = (int) Math.ceil((double) clickedRule.getAmount(clicked) / 2d);
                cursor = cursorRule.apply(clicked, amount);
                clicked = clickedRule.apply(clicked, operand -> operand / 2);
            } else {
                if (clicked.isAir()) {
                    // Put 1 to clicked
                    var tmp = cursor;
                    cursor = cursorRule.apply(tmp, operand -> operand - 1);
                    clicked = clickedRule.apply(tmp, 1);
                } else {
                    // Swap items
                    var tmp = cursor;
                    cursor = clicked;
                    clicked = tmp;
                }
            }
        }
        if (cursor.isAir() && clicked.isAir()) {
            // return empty
            return ClickResultImpl.Single.empty();
        }
        return new ClickResultImpl.Single(cursor, Map.of(slot, clicked));
    }

    public static ClickResult.Single shiftToInventory(Inventory inventory, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();
        // TODO: check inventory type to avoid certain slots (e.g. crafting result)
        var result = TransactionType.ADD.process(inventory, shifted);
        return new ClickResultImpl.Single(result.first(), result.second());
    }

    public static ClickResult.Single shiftToPlayer(PlayerInventory inventory, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();
        // Try shifting from 8->0
        var result = TransactionType.ADD.process(inventory, shifted, (slot, itemStack) -> true, 8, 0, -1);
        var remaining = result.first();
        if (remaining.isAir()) {
            return new ClickResultImpl.Single(result.first(), result.second());
        }
        // Try 35->9
        var result2 = TransactionType.ADD.process(inventory, remaining, (slot, itemStack) -> true, 35, 9, -1);

        remaining = result2.first();
        var changes = result2.second();
        changes.putAll(result.second());
        return new ClickResultImpl.Single(remaining, changes);
    }

    public static ClickResult.Single shiftWithinPlayer(PlayerInventory inventory, int slot, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();

        // Handle equipment
        if (MathUtils.isBetween(slot, 0, 35)) {
            final Material material = shifted.getMaterial();
            final EquipmentSlot equipmentSlot = material.registry().equipmentSlot();
            if (equipmentSlot != null) {
                // Shift-click equip
                final ItemStack currentArmor = inventory.getEquipment(equipmentSlot);
                if (currentArmor.isAir()) {
                    final int armorSlot = equipmentSlot.armorSlot();
                    return new ClickResultImpl.Single(ItemStack.AIR, Map.of(armorSlot, shifted));
                } else {
                    // Equipment already present, do not change anything
                    return new ClickResultImpl.Single(shifted, Map.of());
                }
            }
        }

        // General shift
        if (MathUtils.isBetween(slot, 0, 8)) {
            // Shift from 9->35
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 9, 36, 1);
            return new ClickResultImpl.Single(result.first(), result.second());
        } else if (MathUtils.isBetween(slot, 9, 35)) {
            // Shift from 0->8
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 0, 9, 1);
            return new ClickResultImpl.Single(result.first(), result.second());
        } else {
            // Try shifting from 9->35
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 9, 36, 1);
            var remaining = result.first();
            if (remaining.isAir()) {
                return new ClickResultImpl.Single(result.first(), result.second());
            }
            // Try shifting from 0->8
            var result2 = TransactionType.ADD.process(inventory, remaining, (s, itemStack) -> true, 0, 9, 1);

            remaining = result2.first();
            var changes = result2.second();
            changes.putAll(result.second());
            return new ClickResultImpl.Single(remaining, changes);
        }
    }

    public static ClickResult.Single held(PlayerInventory playerInventory, AbstractInventory inventory,
                                          int clickedSlot, ItemStack clicked,
                                          int heldTarget, ItemStack held) {
        if (playerInventory == inventory && clickedSlot == heldTarget)
            return new ClickResultImpl.Single(clicked, Map.of()); // No change
        if (!MathUtils.isBetween(heldTarget, 0, 8) && heldTarget != PlayerInventoryUtils.OFFHAND_SLOT)
            return new ClickResultImpl.Single(clicked, Map.of()); // Held click is only supported for hotbar and offhand
        // Swap items
        return new ClickResultImpl.Single(held, Map.of(heldTarget, clicked));
    }

    public static ClickResult.Double doubleClick(PlayerInventory playerInventory, Inventory inventory, ItemStack cursor) {
        if (cursor.isAir()) return ClickResultImpl.Double.empty();
        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return new ClickResultImpl.Double(cursor, Map.of(), Map.of());
        }
        ItemStack remaining = cursorRule.apply(cursor, remainingAmount);
        Map<Integer, ItemStack> playerChanges = new HashMap<>();
        Map<Integer, ItemStack> inventoryChanges = new HashMap<>();
        // Loop through open inventory
        {
            // TODO: check inventory type to avoid certain slots (e.g. crafting result)
            var result = TransactionType.TAKE.process(inventory, remaining);
            remaining = result.first();
            inventoryChanges.putAll(result.second());
        }
        // Loop through player inventory
        {
            // 9->36
            if (!remaining.isAir()) {
                var result = TransactionType.TAKE.process(playerInventory, remaining, (slot, itemStack) -> true, 9, 36, 1);
                remaining = result.first();
                playerChanges.putAll(result.second());
            }
            // 8->0
            if (!remaining.isAir()) {
                var result = TransactionType.TAKE.process(playerInventory, remaining, (slot, itemStack) -> true, 8, 0, -1);
                remaining = result.first();
                playerChanges.putAll(result.second());
            }
        }

        // Update cursor based on the remaining
        if (remaining.isAir()) {
            // Item has been filled
            remaining = cursorRule.apply(cursor, maxSize);
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remaining);
            remaining = cursorRule.apply(cursor, amount + tookAmount);
        }
        return new ClickResultImpl.Double(remaining, playerChanges, inventoryChanges);
    }

    public static ClickResult.Single doubleWithinPlayer(PlayerInventory inventory, ItemStack cursor) {
        if (cursor.isAir()) return ClickResultImpl.Single.empty();
        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return new ClickResultImpl.Single(cursor, Map.of());
        }
        ItemStack remaining = cursorRule.apply(cursor, remainingAmount);
        // Try taking from 9->35
        var result = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 9, 36, 1);
        remaining = result.first();
        Map<Integer, ItemStack> changes = result.second();
        // Try 0->8
        if (!remaining.isAir()) {
            var result2 = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 0, 9, 1);
            remaining = result2.first();
            changes.putAll(result2.second());
        }
        // Try 37->40 (crafting slots)
        if (!remaining.isAir()) {
            var result2 = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 37, 41, 1);
            remaining = result2.first();
            changes.putAll(result2.second());
        }

        // Update cursor based on the remaining
        if (remaining.isAir()) {
            // Item has been filled
            remaining = cursorRule.apply(cursor, maxSize);
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remaining);
            remaining = cursorRule.apply(cursor, amount + tookAmount);
        }
        return new ClickResultImpl.Single(remaining, changes);
    }

    public static ClickResult.Double leftDrag(PlayerInventory playerInventory, Inventory inventory,
                                              ItemStack cursor, List<Pair<AbstractInventory, Integer>> slots) {
        if (cursor.isAir()) return ClickResultImpl.Double.empty();
        if (slots.isEmpty()) return new ClickResultImpl.Double(cursor, Map.of(), Map.of());
        final StackingRule stackingRule = cursor.getStackingRule();
        final int cursorAmount = stackingRule.getAmount(cursor);
        final int slotCount = slots.size();
        // Should be size of each defined slot (if not full)
        final int slotSize = Math.max(1, (int) ((float) cursorAmount / (float) slotCount));
        // Place all waiting drag action
        int finalCursorAmount = cursorAmount;

        Map<Integer, ItemStack> playerChanges = new HashMap<>();
        Map<Integer, ItemStack> inventoryChanges = new HashMap<>();
        for (var s : slots) {
            if (finalCursorAmount <= 0)
                break;
            var inv = s.left();
            int slot = s.right();

            ItemStack slotItem = inv.getItemStack(slot);
            final StackingRule slotItemRule = slotItem.getStackingRule();
            final int amount = slotItemRule.getAmount(slotItem);

            boolean mapOp = false;
            if (stackingRule.canBeStacked(cursor, slotItem)) {
                if (stackingRule.canApply(slotItem, amount + slotSize)) {
                    // Append divided amount to slot
                    slotItem = stackingRule.apply(slotItem, a -> a + slotSize);
                    finalCursorAmount -= slotSize;
                } else {
                    // Amount too big, fill as much as possible
                    final int maxSize = stackingRule.getMaxSize(cursor);
                    final int removedAmount = maxSize - amount;
                    if (removedAmount <= 0)
                        continue;
                    slotItem = stackingRule.apply(slotItem, maxSize);
                    finalCursorAmount -= removedAmount;
                }
                mapOp = true;
            } else if (slotItem.isAir()) {
                // Slot is empty, add divided amount
                slotItem = stackingRule.apply(cursor, slotSize);
                finalCursorAmount -= slotSize;

                mapOp = true;
            }

            if (mapOp) {
                if (inv == playerInventory) {
                    playerChanges.put(slot, slotItem);
                } else if (inv == inventory) {
                    inventoryChanges.put(slot, slotItem);
                } else {
                    throw new IllegalStateException("Unknown inventory: " + inv);
                }
            }
        }
        return new ClickResultImpl.Double(stackingRule.apply(cursor, finalCursorAmount), playerChanges, inventoryChanges);
    }

    public static ClickResult.Single leftDragWithinPlayer(PlayerInventory inventory, ItemStack cursor, List<Integer> slots) {
        if (cursor.isAir()) return ClickResultImpl.Single.empty();
        if (slots.isEmpty()) return new ClickResultImpl.Single(cursor, Map.of());
        final StackingRule stackingRule = cursor.getStackingRule();
        final int cursorAmount = stackingRule.getAmount(cursor);
        final int slotCount = slots.size();
        // Should be size of each defined slot (if not full)
        final int slotSize = Math.max(1, (int) ((float) cursorAmount / (float) slotCount));
        // Place all waiting drag action
        int finalCursorAmount = cursorAmount;

        Map<Integer, ItemStack> changes = new HashMap<>();
        for (int slot : slots) {
            if (finalCursorAmount <= 0)
                break;
            ItemStack slotItem = inventory.getItemStack(slot);
            final StackingRule slotItemRule = slotItem.getStackingRule();
            final int amount = slotItemRule.getAmount(slotItem);
            if (stackingRule.canBeStacked(cursor, slotItem)) {
                if (stackingRule.canApply(slotItem, amount + slotSize)) {
                    // Append divided amount to slot
                    slotItem = stackingRule.apply(slotItem, a -> a + slotSize);
                    finalCursorAmount -= slotSize;
                } else {
                    // Amount too big, fill as much as possible
                    final int maxSize = stackingRule.getMaxSize(cursor);
                    final int removedAmount = maxSize - amount;
                    if (removedAmount <= 0)
                        continue;
                    slotItem = stackingRule.apply(slotItem, maxSize);
                    finalCursorAmount -= removedAmount;
                }
                changes.put(slot, slotItem);
            } else if (slotItem.isAir()) {
                // Slot is empty, add divided amount
                slotItem = stackingRule.apply(cursor, slotSize);
                finalCursorAmount -= slotSize;

                changes.put(slot, slotItem);
            }
        }
        return new ClickResultImpl.Single(stackingRule.apply(cursor, finalCursorAmount), changes);
    }

    public static ClickResult.Double rightDrag(PlayerInventory playerInventory, Inventory inventory,
                                               ItemStack cursor, List<Pair<AbstractInventory, Integer>> slots) {
        if (cursor.isAir()) return ClickResultImpl.Double.empty();
        if (slots.isEmpty()) return new ClickResultImpl.Double(cursor, Map.of(), Map.of());
        final StackingRule stackingRule = cursor.getStackingRule();
        // Place all waiting drag action
        int finalCursorAmount = stackingRule.getAmount(cursor);

        Map<Integer, ItemStack> playerChanges = new HashMap<>();
        Map<Integer, ItemStack> inventoryChanges = new HashMap<>();
        for (var s : slots) {
            if (finalCursorAmount <= 0)
                break;
            var inv = s.left();
            int slot = s.right();
            boolean mapOp = false;

            ItemStack slotItem = inv.getItemStack(slot);
            StackingRule slotItemRule = slotItem.getStackingRule();
            if (stackingRule.canBeStacked(cursor, slotItem)) {
                // Compatible item in the slot, increment by 1
                final int amount = slotItemRule.getAmount(slotItem) + 1;
                if (stackingRule.canApply(slotItem, amount)) {
                    slotItem = stackingRule.apply(slotItem, amount);
                    finalCursorAmount -= 1;
                    mapOp = true;
                }
            } else if (slotItem.isAir()) {
                // No item at the slot, place one
                slotItem = stackingRule.apply(cursor, 1);
                finalCursorAmount -= 1;
                mapOp = true;
            }

            if (mapOp) {
                if (inv == playerInventory) {
                    playerChanges.put(slot, slotItem);
                } else if (inv == inventory) {
                    inventoryChanges.put(slot, slotItem);
                } else {
                    throw new IllegalStateException("Unknown inventory: " + inv);
                }
            }
        }
        return new ClickResultImpl.Double(stackingRule.apply(cursor, finalCursorAmount), playerChanges, inventoryChanges);
    }

    public static ClickResult.Single rightDragWithinPlayer(PlayerInventory inventory, ItemStack cursor, List<Integer> slots) {
        if (cursor.isAir()) return ClickResultImpl.Single.empty();
        if (slots.isEmpty()) return new ClickResultImpl.Single(cursor, Map.of());
        final StackingRule stackingRule = cursor.getStackingRule();
        // Place all waiting drag action
        int finalCursorAmount = stackingRule.getAmount(cursor);

        Map<Integer, ItemStack> changes = new HashMap<>();
        for (int slot : slots) {
            if (finalCursorAmount <= 0)
                break;
            ItemStack slotItem = inventory.getItemStack(slot);
            StackingRule slotItemRule = slotItem.getStackingRule();
            if (stackingRule.canBeStacked(cursor, slotItem)) {
                // Compatible item in the slot, increment by 1
                final int amount = slotItemRule.getAmount(slotItem) + 1;
                if (stackingRule.canApply(slotItem, amount)) {
                    slotItem = stackingRule.apply(slotItem, amount);
                    finalCursorAmount -= 1;
                    changes.put(slot, slotItem);
                }
            } else if (slotItem.isAir()) {
                // No item at the slot, place one
                slotItem = stackingRule.apply(cursor, 1);
                finalCursorAmount -= 1;
                changes.put(slot, slotItem);
            }
        }
        return new ClickResultImpl.Single(stackingRule.apply(cursor, finalCursorAmount), changes);
    }

    public static ClickResult.Drop drop(boolean all, int slot, int button,
                                        @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final StackingRule clickedRule = clicked.getStackingRule();
        final StackingRule cursorRule = cursor.getStackingRule();

        if (slot == -999) {
            // Click outside
            if (button == 0) {
                // Left (drop all)
                return new ClickResultImpl.Drop(ItemStack.AIR, cursor);
            } else if (button == 1) {
                // Right (drop 1)
                final int remainingAmount = cursorRule.getAmount(cursor) - 1;
                final ItemStack remaining = cursorRule.apply(cursor, remainingAmount);
                final ItemStack dropItem = cursorRule.apply(cursor, 1);
                return new ClickResultImpl.Drop(remaining, dropItem);
            }
        } else if (!all) {
            if (button == 0) {
                // Drop key Q (drop 1)
                final int remainingAmount = clickedRule.getAmount(clicked) - 1;
                final ItemStack remaining = clickedRule.apply(clicked, remainingAmount);
                final ItemStack dropItem = cursorRule.apply(clicked, 1);
                return new ClickResultImpl.Drop(remaining, dropItem);
            } else if (button == 1) {
                // Ctrl + Drop key Q (drop all)
                return new ClickResultImpl.Drop(ItemStack.AIR, clicked);
            }
        }
        throw new IllegalStateException("Unknown drop: " + button);
    }
}
