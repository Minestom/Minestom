package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class InventoryClickProcessor {

    // Dragging maps
    private final Map<Player, IntSet> leftDraggingMap = new HashMap<>();
    private final Map<Player, IntSet> rightDraggingMap = new HashMap<>();

    public InventoryClickResult leftClick(Inventory inventory, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.LEFT_CLICK, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorRule.canBeStacked(cursor, clicked)) {

            resultCursor = cursor.clone();
            resultClicked = clicked.clone();

            final int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);

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

    public InventoryClickResult rightClick(Inventory inventory, Player player, int slot, ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.RIGHT_CLICK, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir() && clicked.isAir()) {
            clickResult.setCancel(true);
            return clickResult;
        }

        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (clickedRule.canBeStacked(clicked, cursor)) {
            resultClicked = clicked.clone();
            final int amount = clicked.getAmount() + 1;
            if (!clickedRule.canApply(resultClicked, amount)) {
                return clickResult;
            } else {
                resultCursor = cursor.clone();
                resultCursor = cursorRule.apply(resultCursor, cursorRule.getAmount(resultCursor) - 1);
                resultClicked = clickedRule.apply(resultClicked, amount);
            }
        } else {
            if (cursor.isAir()) {
                final int amount = (int) Math.ceil((double) clicked.getAmount() / 2d);
                resultCursor = clicked.clone();
                resultCursor = cursorRule.apply(resultCursor, amount);

                resultClicked = clicked.clone();
                resultClicked = clickedRule.apply(resultClicked, clicked.getAmount() / 2);
            } else {
                if (clicked.isAir()) {
                    final int amount = cursor.getAmount();
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

    public InventoryClickResult changeHeld(Inventory inventory, Player player, int slot, int key, ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.CHANGE_HELD, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        // Converted again during the inventory condition calling to internal slot
        final int keySlot = PlayerInventoryUtils.convertToPacketSlot(key);
        clickResult = startCondition(clickResult, null, player, keySlot, ClickType.CHANGE_HELD, clicked, cursor);

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
            resultClicked = cursor.clone();
            resultHeld = ItemStack.getAirItem();
        } else {
            if (cursor.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.getAirItem();
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

    public InventoryClickResult shiftClick(Inventory inventory, Player player, int slot,
                                           ItemStack clicked, ItemStack cursor, InventoryClickLoopHandler... loopHandlers) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (clicked.isAir())
            return null;

        final StackingRule clickedRule = clicked.getStackingRule();

        boolean filled = false;
        ItemStack resultClicked = clicked.clone();

        for (InventoryClickLoopHandler loopHandler : loopHandlers) {
            final Int2IntFunction indexModifier = loopHandler.getIndexModifier();
            final Int2ObjectFunction<ItemStack> itemGetter = loopHandler.getItemGetter();
            final BiConsumer<Integer, ItemStack> itemSetter = loopHandler.getItemSetter();

            for (int i = loopHandler.getStart(); i < loopHandler.getEnd(); i += loopHandler.getStep()) {
                final int index = indexModifier.apply(i);
                if (index == slot)
                    continue;

                ItemStack item = itemGetter.apply(index);
                final StackingRule itemRule = item.getStackingRule();
                if (itemRule.canBeStacked(item, clicked)) {

                    clickResult = startCondition(clickResult, inventory, player, index, ClickType.SHIFT_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        continue;

                    final int amount = itemRule.getAmount(item);
                    if (!clickedRule.canApply(clicked, amount + 1))
                        continue;

                    int totalAmount = clickedRule.getAmount(resultClicked) + amount;
                    if (!clickedRule.canApply(clicked, totalAmount)) {
                        item = itemRule.apply(item, itemRule.getMaxSize());
                        itemSetter.accept(index, item);

                        resultClicked = clickedRule.apply(resultClicked, totalAmount - clickedRule.getMaxSize());
                        filled = false;

                        callClickEvent(player, inventory, index, ClickType.SHIFT_CLICK, item, cursor);
                        continue;
                    } else {
                        resultClicked = clickedRule.apply(resultClicked, totalAmount);
                        itemSetter.accept(index, resultClicked);

                        item = itemRule.apply(item, 0);
                        itemSetter.accept(slot, item);
                        filled = true;

                        callClickEvent(player, inventory, index, ClickType.SHIFT_CLICK, item, cursor);
                        break;
                    }
                } else if (item.isAir()) {

                    clickResult = startCondition(clickResult, inventory, player, index, ClickType.SHIFT_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        continue;

                    // Switch
                    itemSetter.accept(index, resultClicked);
                    itemSetter.accept(slot, ItemStack.getAirItem());
                    filled = true;
                    break;
                }
            }
            if (!filled) {
                itemSetter.accept(slot, resultClicked);
            }
        }

        return clickResult;
    }

    public InventoryClickResult dragging(Inventory inventory, Player player,
                                         int slot, int button,
                                         ItemStack clicked, ItemStack cursor,
                                         Int2ObjectFunction<ItemStack> itemGetter,
                                         BiConsumer<Integer, ItemStack> itemSetter) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);

        final StackingRule stackingRule = cursor.getStackingRule();

        if (slot == -999) {
            // Start or end left/right drag
            if (button == 0) {
                // Start left
                this.leftDraggingMap.put(player, new IntOpenHashSet());
            } else if (button == 4) {
                // Start right
                this.rightDraggingMap.put(player, new IntOpenHashSet());
            } else if (button == 2) {
                // End left
                if (!leftDraggingMap.containsKey(player))
                    return null;
                final Set<Integer> slots = leftDraggingMap.get(player);
                final int slotCount = slots.size();
                final int cursorAmount = stackingRule.getAmount(cursor);
                if (slotCount > cursorAmount)
                    return null;
                // Should be size of each defined slot (if not full)
                final int slotSize = (int) ((float) cursorAmount / (float) slotCount);
                int finalCursorAmount = cursorAmount;

                for (Integer s : slots) {
                    ItemStack draggedItem = cursor.clone();
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(clickResult, inventory, player, s, ClickType.DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel())
                        continue;

                    final int maxSize = stackingRule.getMaxSize();
                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        final int amount = slotItem.getAmount() + slotSize;
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

                    callClickEvent(player, inventory, s, ClickType.DRAGGING, slotItem, cursor);
                }
                cursor = stackingRule.apply(cursor, finalCursorAmount);
                clickResult.setCursor(cursor);

                leftDraggingMap.remove(player);
            } else if (button == 6) {
                // End right
                if (!rightDraggingMap.containsKey(player))
                    return null;
                final Set<Integer> slots = rightDraggingMap.get(player);
                final int size = slots.size();
                int cursorAmount = stackingRule.getAmount(cursor);
                if (size > cursorAmount)
                    return null;
                for (Integer s : slots) {
                    ItemStack draggedItem = cursor.clone();
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(clickResult, inventory, player, s, ClickType.DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel())
                        continue;

                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        final int amount = slotItem.getAmount() + 1;
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

                    callClickEvent(player, inventory, s, ClickType.DRAGGING, draggedItem, cursor);
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

    public InventoryClickResult doubleClick(Inventory inventory, Player player, int slot,
                                            ItemStack cursor, InventoryClickLoopHandler... loopHandlers) {
        InventoryClickResult clickResult = new InventoryClickResult(ItemStack.getAirItem(), cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir())
            return null;

        final StackingRule cursorRule = cursor.getStackingRule();
        int amount = cursorRule.getAmount(cursor);

        if (!cursorRule.canApply(cursor, amount + 1))
            return null;

        for (InventoryClickLoopHandler loopHandler : loopHandlers) {
            final Int2IntFunction indexModifier = loopHandler.getIndexModifier();
            final Int2ObjectFunction<ItemStack> itemGetter = loopHandler.getItemGetter();
            final BiConsumer<Integer, ItemStack> itemSetter = loopHandler.getItemSetter();

            for (int i = loopHandler.getStart(); i < loopHandler.getEnd(); i += loopHandler.getStep()) {
                final int index = indexModifier.apply(i);
                if (index == slot)
                    continue;

                ItemStack item = itemGetter.apply(index);
                final StackingRule itemRule = item.getStackingRule();
                if (!cursorRule.canApply(cursor, amount + 1))
                    break;
                if (cursorRule.canBeStacked(cursor, item)) {
                    clickResult = startCondition(clickResult, inventory, player, index, ClickType.DOUBLE_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        continue;

                    final int totalAmount = amount + cursorRule.getAmount(item);
                    if (!cursorRule.canApply(cursor, totalAmount)) {
                        cursor = cursorRule.apply(cursor, cursorRule.getMaxSize());

                        item = itemRule.apply(item, totalAmount - itemRule.getMaxSize());
                    } else {
                        cursor = cursorRule.apply(cursor, totalAmount);
                        item = itemRule.apply(item, 0);
                    }
                    itemSetter.accept(index, item);
                    amount = cursorRule.getAmount(cursor);

                    callClickEvent(player, inventory, index, ClickType.DOUBLE_CLICK, item, cursor);
                }
            }
        }

        clickResult.setCursor(cursor);

        return clickResult;
    }

    public InventoryClickResult drop(Inventory inventory, Player player,
                                     int mode, int slot, int button,
                                     ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.DROP, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        final StackingRule clickedRule = clicked == null ? null : clicked.getStackingRule();
        final StackingRule cursorRule = cursor.getStackingRule();

        ItemStack resultClicked = clicked == null ? null : clicked.clone();
        ItemStack resultCursor = cursor.clone();


        if (slot == -999) {
            // Click outside
            if (button == 0) {
                // Left (drop all)
                final int amount = cursorRule.getAmount(resultCursor);
                final ItemStack dropItem = cursorRule.apply(resultCursor.clone(), amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultCursor = cursorRule.apply(resultCursor, 0);
                }
            } else if (button == 1) {
                // Right (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultCursor.clone(), 1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = cursorRule.getAmount(resultCursor);
                    final int newAmount = amount - 1;
                    resultCursor = cursorRule.apply(resultCursor, newAmount);
                }
            }

        } else if (mode == 4) {
            if (button == 0) {
                // Drop key Q (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultClicked.clone(), 1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = clickedRule.getAmount(resultClicked);
                    final int newAmount = amount - 1;
                    resultClicked = cursorRule.apply(resultClicked, newAmount);
                }
            } else if (button == 1) {
                // Ctrl + Drop key Q (drop all)
                final int amount = cursorRule.getAmount(resultClicked);
                final ItemStack dropItem = clickedRule.apply(resultClicked.clone(), amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultClicked = cursorRule.apply(resultClicked, 0);
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    private InventoryClickResult startCondition(InventoryClickResult clickResult, Inventory inventory,
                                                Player player, int slot, ClickType clickType,
                                                ItemStack clicked, ItemStack cursor) {
        boolean isPlayerInventory = inventory == null;
        final int inventorySlot = isPlayerInventory ? 0 : inventory.getSize();
        isPlayerInventory = isPlayerInventory ? isPlayerInventory : slot >= inventorySlot;

        clickResult.setPlayerInventory(isPlayerInventory);

        if (isPlayerInventory && inventory != null) {
            slot = slot - inventorySlot + PlayerInventoryUtils.OFFSET;
        }

        {
            if (clicked != null) {
                clicked.onInventoryClick(player, clickType, slot, isPlayerInventory);
            }
        }

        // Reset the didCloseInventory field
        // Wait for inventory conditions + events to possibly close the inventory
        player.UNSAFE_changeDidCloseInventory(false);

        // PreClickEvent
        {
            InventoryPreClickEvent inventoryPreClickEvent = new InventoryPreClickEvent(player, inventory, slot, clickType, clicked, cursor);
            player.callEvent(InventoryPreClickEvent.class, inventoryPreClickEvent);
            cursor = inventoryPreClickEvent.getCursorItem();
            clicked = inventoryPreClickEvent.getClickedItem();

            clickResult.setCancel(inventoryPreClickEvent.isCancelled());
            if (inventoryPreClickEvent.isCancelled()) {
                clickResult.setRefresh(true);
            }
        }

        // Inventory conditions
        final List<InventoryCondition> inventoryConditions = isPlayerInventory ?
                player.getInventory().getInventoryConditions() : inventory.getInventoryConditions();
        if (!inventoryConditions.isEmpty()) {

            for (InventoryCondition inventoryCondition : inventoryConditions) {
                InventoryConditionResult result = new InventoryConditionResult(clicked, cursor);
                inventoryCondition.accept(player, slot, clickType, result);

                cursor = result.getCursorItem();
                clicked = result.getClickedItem();

                clickResult.setCursor(cursor);
                clickResult.setClicked(clicked);

                clickResult.setCancel(result.isCancel());
                if (result.isCancel()) {
                    clickResult.setRefresh(true);
                }
            }

            // Cancel the click if the inventory has been closed by Player#closeInventory within an inventory listener
            if (player.didCloseInventory()) {
                clickResult.setCancel(true);
                player.UNSAFE_changeDidCloseInventory(false);
            }


        }
        return clickResult;
    }

    private InventoryClickResult startCondition(Inventory inventory, Player player, int slot, ClickType clickType, ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        return startCondition(clickResult, inventory, player, slot, clickType, clicked, cursor);
    }

    private void callClickEvent(Player player, Inventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(player, inventory, slot, clickType, clicked, cursor);
        player.callEvent(InventoryClickEvent.class, inventoryClickEvent);
    }

    public void clearCache(Player player) {
        this.leftDraggingMap.remove(player);
        this.rightDraggingMap.remove(player);
    }

}
