package fr.themode.minestom.inventory.click;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.condition.InventoryCondition;
import fr.themode.minestom.inventory.condition.InventoryConditionResult;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class InventoryClickProcessor {

    // Dragging maps
    private Map<Player, Set<Integer>> leftDraggingMap = new HashMap<>();
    private Map<Player, Set<Integer>> rightDraggingMap = new HashMap<>();

    public InventoryClickResult leftClick(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        StackingRule cursorRule = cursor.getStackingRule();
        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorRule.canBeStacked(cursor, clicked)) {

            resultCursor = cursor.clone();
            resultClicked = clicked.clone();

            int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);

            if (!clickedRule.canApply(resultClicked, totalAmount)) {
                resultCursor = cursorRule.apply(resultCursor, totalAmount - cursorRule.getMaxSize());
                resultClicked = clickedRule.apply(resultClicked, clickedRule.getMaxSize());
            } else {
                resultCursor = cursorRule.apply(resultCursor, 0);
                resultClicked = clickedRule.apply(resultClicked, totalAmount);
            }
        } else {
            resultCursor = clicked.clone();
            resultClicked = cursor.clone();
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    public InventoryClickResult rightClick(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        StackingRule cursorRule = cursor.getStackingRule();
        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (clickedRule.canBeStacked(clicked, cursor)) {
            resultClicked = clicked.clone();
            int amount = clicked.getAmount() + 1;
            if (!clickedRule.canApply(resultClicked, amount)) {
                return clickResult;
            } else {
                resultCursor = cursor.clone();
                resultCursor = cursorRule.apply(resultCursor, cursorRule.getAmount(resultCursor) - 1);
                resultClicked = clickedRule.apply(resultClicked, amount);
            }
        } else {
            if (cursor.isAir()) {
                int amount = (int) Math.ceil((double) clicked.getAmount() / 2d);
                resultCursor = clicked.clone();
                resultCursor = cursorRule.apply(resultCursor, amount);

                resultClicked = clicked.clone();
                resultClicked = clickedRule.apply(resultClicked, clicked.getAmount() / 2);
            } else {
                if (clicked.isAir()) {
                    int amount = cursor.getAmount();
                    resultCursor = cursor.clone();
                    resultCursor = cursorRule.apply(resultCursor, amount - 1);

                    resultClicked = cursor.clone();
                    resultClicked = clickedRule.apply(resultClicked, 1);
                } else {
                    resultCursor = clicked.clone();
                    resultClicked = cursor.clone();
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    public InventoryClickResult changeHeld(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventoryCondition, player, slot, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        ItemStack resultClicked;
        ItemStack resultHeld;

        if (clicked.isAir()) {
            // Set held item [key] to slot
            resultClicked = ItemStack.AIR_ITEM;
            resultHeld = clicked.clone();
        } else {
            if (cursor.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.AIR_ITEM;
                resultHeld = clicked.clone();
            } else {
                // Otherwise replace held item and held
                resultClicked = cursor.clone();
                resultHeld = clicked.clone();
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultHeld);

        return clickResult;
    }

    public InventoryClickResult dragging(InventoryCondition inventoryCondition, Player player,
                                         int slot, int button,
                                         ItemStack clicked, ItemStack cursor,
                                         Function<Integer, ItemStack> itemGetter,
                                         BiConsumer<Integer, ItemStack> itemSetter) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);

        StackingRule stackingRule = cursor.getStackingRule();

        if (slot == -999) {
            // Start or end left/right drag
            if (button == 0) {
                // Start left
                this.leftDraggingMap.put(player, new HashSet<>());
            } else if (button == 4) {
                // Start right
                this.rightDraggingMap.put(player, new HashSet<>());
            } else if (button == 2) {
                // End left
                if (!leftDraggingMap.containsKey(player))
                    return null;
                Set<Integer> slots = leftDraggingMap.get(player);
                int slotCount = slots.size();
                int cursorAmount = stackingRule.getAmount(cursor);
                if (slotCount > cursorAmount)
                    return null;
                // Should be size of each defined slot (if not full)
                int slotSize = (int) ((float) cursorAmount / (float) slotCount);
                int finalCursorAmount = cursorAmount;

                for (Integer s : slots) {
                    ItemStack draggedItem = cursor.clone();
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(clickResult, inventoryCondition, player, s, slotItem, cursor);
                    if (clickResult.isCancel())
                        continue;

                    int maxSize = stackingRule.getMaxSize();
                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        int amount = slotItem.getAmount() + slotSize;
                        if (stackingRule.canApply(slotItem, amount)) {
                            slotItem = stackingRule.apply(slotItem, amount);
                            finalCursorAmount -= slotSize;
                        } else {
                            int removedAmount = amount - maxSize;
                            slotItem = stackingRule.apply(slotItem, maxSize);
                            finalCursorAmount -= removedAmount;
                        }
                    } else if (slotItem.isAir()) {
                        slotItem = stackingRule.apply(draggedItem, slotSize);
                        finalCursorAmount -= slotSize;
                    }
                    itemSetter.accept(s, slotItem);
                }
                cursor = stackingRule.apply(cursor, finalCursorAmount);
                clickResult.setCursor(cursor);

                leftDraggingMap.remove(player);
            } else if (button == 6) {
                // End right
                if (!rightDraggingMap.containsKey(player))
                    return null;
                Set<Integer> slots = rightDraggingMap.get(player);
                int size = slots.size();
                int cursorAmount = stackingRule.getAmount(cursor);
                if (size > cursorAmount)
                    return null;
                for (Integer s : slots) {
                    ItemStack draggedItem = cursor.clone();
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(clickResult, inventoryCondition, player, s, slotItem, cursor);
                    if (clickResult.isCancel())
                        continue;

                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        int amount = slotItem.getAmount() + 1;
                        if (stackingRule.canApply(slotItem, amount)) {
                            slotItem = stackingRule.apply(slotItem, amount);
                            itemSetter.accept(s, slotItem);
                            cursorAmount -= 1;
                        }
                    } else if (slotItem.isAir()) {
                        draggedItem = stackingRule.apply(draggedItem, 1);
                        itemSetter.accept(s, draggedItem);
                        cursorAmount -= 1;
                    }
                }
                cursor = stackingRule.apply(cursor, cursorAmount);
                clickResult.setCursor(cursor);

                rightDraggingMap.remove(player);

            }
        } else {
            // Add slot
            if (button == 1) {
                // Add left slot
                if (!leftDraggingMap.containsKey(player))
                    return null;
                leftDraggingMap.get(player).add(slot);

            } else if (button == 5) {
                // Add right slot
                if (!rightDraggingMap.containsKey(player))
                    return null;
                rightDraggingMap.get(player).add(slot);
            }
        }

        return clickResult;
    }

    public InventoryClickResult doubleClick(InventoryCondition inventoryCondition, Player player, int slot,
                                            ItemStack cursor, InventoryClickLoopHandler... loopHandlers) {
        InventoryClickResult clickResult = new InventoryClickResult(ItemStack.AIR_ITEM, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir())
            return null;

        StackingRule cursorRule = cursor.getStackingRule();
        int amount = cursorRule.getAmount(cursor);

        if (!cursorRule.canApply(cursor, amount + 1))
            return null;

        for (InventoryClickLoopHandler loopHandler : loopHandlers) {
            Function<Integer, Integer> indexModifier = loopHandler.getIndexModifier();
            Function<Integer, ItemStack> itemGetter = loopHandler.getItemGetter();
            BiConsumer<Integer, ItemStack> itemSetter = loopHandler.getItemSetter();

            for (int i = loopHandler.getStart(); i < loopHandler.getEnd(); i += loopHandler.getStep()) {
                int index = indexModifier.apply(i);
                if (index == slot)
                    continue;

                ItemStack item = itemGetter.apply(index);
                StackingRule itemRule = item.getStackingRule();
                if (!cursorRule.canApply(cursor, amount + 1))
                    break;
                if (cursorRule.canBeStacked(cursor, item)) {
                    clickResult = startCondition(clickResult, inventoryCondition, player, index, item, cursor);
                    if (clickResult.isCancel())
                        continue;

                    int totalAmount = amount + cursorRule.getAmount(item);
                    if (!cursorRule.canApply(cursor, totalAmount)) {
                        cursor = cursorRule.apply(cursor, cursorRule.getMaxSize());

                        item = itemRule.apply(item, totalAmount - itemRule.getMaxSize());
                    } else {
                        cursor = cursorRule.apply(cursor, totalAmount);
                        item = itemRule.apply(item, 0);
                    }
                    itemSetter.accept(index, item);
                    amount = cursorRule.getAmount(cursor);
                }
            }
        }

        clickResult.setCursor(cursor);

        return clickResult;
    }

    private InventoryClickResult startCondition(InventoryClickResult clickResult, InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursor);
            inventoryCondition.accept(player, slot, result);

            cursor = result.getCursorItem();
            clicked = result.getClickedItem();

            clickResult.setCancel(result.isCancel());
            if (result.isCancel()) {
                clickResult.setClicked(clicked);
                clickResult.setCursor(cursor);
                clickResult.setRefresh(true);
            }
        }
        return clickResult;
    }

    private InventoryClickResult startCondition(InventoryCondition inventoryCondition, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        return startCondition(clickResult, inventoryCondition, player, slot, clicked, cursor);
    }

}
