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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class InventoryClickProcessor {

    // Dragging maps
    private final Map<Player, IntSet> leftDraggingMap = new HashMap<>();
    private final Map<Player, IntSet> rightDraggingMap = new HashMap<>();

    @NotNull
    public InventoryClickResult leftClick(@Nullable Inventory inventory, @NotNull Player player, int slot,
                                          @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.LEFT_CLICK, clicked, cursor);
        clicked = clickResult.getClicked();
        cursor = clickResult.getCursor();

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
            final int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);

            if (!clickedRule.canApply(clicked, totalAmount)) {
                var split = cursorRule.split(cursor, totalAmount - cursorRule.getMaxSize());
                resultCursor = split.left();
                resultClicked = clickedRule.merge(clicked, split.right());
            } else {
                var split = cursorRule.split(cursor, 0);
                resultCursor = split.left();
                resultClicked = clickedRule.merge(clicked, split.right());
            }
        } else {
            resultCursor = clicked;
            resultClicked = cursor;
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    @NotNull
    public InventoryClickResult rightClick(@Nullable Inventory inventory, @NotNull Player player, int slot,
                                           @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
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
            final int amount = clickedRule.getAmount(clicked) + 1;
            if (!clickedRule.canApply(clicked, amount)) {
                return clickResult;
            } else {
                var split = cursorRule.split(cursor, 1);
                resultCursor = split.right();
                resultClicked = clickedRule.merge(clicked, split.left());
            }
        } else {
            if (cursor.isAir()) {
                var split = clickedRule.split(clicked, clickedRule.getAmount(clicked) >> 1);
                resultClicked = split.left();
                resultCursor = split.right();
            } else {
                if (clicked.isAir()) {
                    var split = cursorRule.split(cursor, 1);
                    resultClicked = split.left();
                    resultCursor = split.right();
                } else {
                    resultCursor = clicked;
                    resultClicked = cursor;
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    @NotNull
    public InventoryClickResult changeHeld(@Nullable Inventory inventory, @NotNull Player player, int slot, int key,
                                           @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.CHANGE_HELD, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        // Converted again during the inventory condition calling to internal slot
        final int keySlot = PlayerInventoryUtils.convertToPacketSlot(key);
        clickResult = startCondition(null, player, keySlot, ClickType.CHANGE_HELD, clicked, cursor);

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
            resultClicked = cursor;
            resultHeld = ItemStack.AIR;
        } else {
            if (cursor.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.AIR;
            } else {
                // Otherwise replace held item and held
                resultClicked = cursor;
            }

            resultHeld = clicked;
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultHeld);

        return clickResult;
    }

    @Nullable
    public InventoryClickResult shiftClick(@Nullable Inventory inventory, @NotNull Player player, int slot,
                                           @NotNull ItemStack clicked, @NotNull ItemStack cursor, @NotNull InventoryClickLoopHandler... loopHandlers) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.START_SHIFT_CLICK, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (clicked.isAir())
            return null;

        final StackingRule clickedRule = clicked.getStackingRule();

        boolean filled = false;
        ItemStack resultClicked = clicked;

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

                    clickResult = startCondition(inventory, player, index, ClickType.SHIFT_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        break;

                    final int amount = itemRule.getAmount(item);
                    if (!clickedRule.canApply(clicked, amount + 1))
                        continue;

                    final int totalAmount = clickedRule.getAmount(resultClicked) + amount;
                    if (!clickedRule.canApply(clicked, totalAmount)) {
                        var split = clickedRule.split(resultClicked, itemRule.getMaxSize() - amount);
                        item = itemRule.merge(item, split.left());
                        itemSetter.accept(index, item);

                        resultClicked = split.right();
                        filled = false;

                        callClickEvent(player, inventory, index, ClickType.SHIFT_CLICK, item, cursor);
                    } else {
                        resultClicked = clickedRule.merge(resultClicked, item);
                        itemSetter.accept(index, resultClicked);

                        item = itemRule.split(item, 0).left();
                        itemSetter.accept(slot, item);
                        filled = true;

                        callClickEvent(player, inventory, index, ClickType.SHIFT_CLICK, item, cursor);
                        break;
                    }
                } else if (item.isAir()) {

                    clickResult = startCondition(inventory, player, index, ClickType.SHIFT_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        break;

                    // Switch
                    itemSetter.accept(index, resultClicked);
                    itemSetter.accept(slot, ItemStack.AIR);
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

    @Nullable
    public InventoryClickResult dragging(@Nullable Inventory inventory, @NotNull Player player,
                                         int slot, int button,
                                         @NotNull ItemStack clicked, @NotNull ItemStack cursor,
                                         @NotNull Int2ObjectFunction<ItemStack> itemGetter,
                                         @NotNull BiConsumer<Integer, ItemStack> itemSetter) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.START_DRAGGING, clicked, cursor);

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
                final IntSet slots = leftDraggingMap.get(player);
                final int slotCount = slots.size();
                final int cursorAmount = stackingRule.getAmount(cursor);
                if (slotCount > cursorAmount)
                    return null;
                // Should be size of each defined slot (if not full)
                final int slotSize = (int) ((float) cursorAmount / (float) slotCount);

                for (int s : slots) {
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(inventory, player, s, ClickType.DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel())
                        break;
                    StackingRule slotItemRule = slotItem.getStackingRule();

                    final int maxSize = stackingRule.getMaxSize();
                    if (stackingRule.canBeStacked(cursor, slotItem)) {
                        final int amount = slotItemRule.getAmount(slotItem) + slotSize;
                        if (stackingRule.canApply(slotItem, amount)) {
                            var split = stackingRule.split(cursor, slotSize);
                            slotItem = stackingRule.merge(slotItem, split.left());
                            cursor = split.right();
                        } else {
                            var split = stackingRule.split(cursor, amount - maxSize);
                            slotItem = stackingRule.merge(slotItem, split.left());
                            cursor = split.right();
                        }
                    } else if (slotItem.isAir()) {
                        var split = stackingRule.split(cursor, slotSize);
                        slotItem = split.left();
                        cursor = split.right();
                    }
                    itemSetter.accept(s, slotItem);

                    callClickEvent(player, inventory, s, ClickType.DRAGGING, slotItem, cursor);
                }
                clickResult.setCursor(cursor);

                leftDraggingMap.remove(player);
            } else if (button == 6) {
                // End right
                if (!rightDraggingMap.containsKey(player))
                    return null;
                final IntSet slots = rightDraggingMap.get(player);
                final int size = slots.size();
                if (size > stackingRule.getAmount(cursor))
                    return null;
                for (int s : slots) {
                    ItemStack draggedItem = cursor;
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(inventory, player, s, ClickType.DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel())
                        break;

                    StackingRule slotItemRule = slotItem.getStackingRule();
                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        final int amount = slotItemRule.getAmount(slotItem) + 1;
                        if (stackingRule.canApply(slotItem, amount)) {
                            var split = stackingRule.split(cursor, 1);
                            slotItem = stackingRule.merge(slotItem, split.left());
                            itemSetter.accept(s, slotItem);
                            cursor = split.right();
                        }
                    } else if (slotItem.isAir()) {
                        var split = stackingRule.split(cursor, 1);
                        draggedItem = split.left();
                        itemSetter.accept(s, draggedItem);
                        cursor = split.right();
                    }

                    callClickEvent(player, inventory, s, ClickType.DRAGGING, draggedItem, cursor);
                }
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

    @Nullable
    public InventoryClickResult doubleClick(@Nullable Inventory inventory, @NotNull Player player, int slot,
                                            @NotNull ItemStack cursor, @NotNull InventoryClickLoopHandler... loopHandlers) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.START_DOUBLE_CLICK, ItemStack.AIR, cursor);

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
                    clickResult = startCondition(inventory, player, index, ClickType.DOUBLE_CLICK, item, cursor);
                    if (clickResult.isCancel())
                        break;

                    final int totalAmount = amount + cursorRule.getAmount(item);
                    if (!cursorRule.canApply(cursor, totalAmount)) {
                        var split = itemRule.split(item, cursorRule.getMaxSize() - amount);
                        cursor = cursorRule.merge(cursor, split.left());
                        item = split.right();
                    } else {
                        var split = itemRule.split(item, 0);
                        cursor = cursorRule.merge(cursor, split.right());
                        item = split.left();
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

    @NotNull
    public InventoryClickResult drop(@Nullable Inventory inventory, @NotNull Player player,
                                     int mode, int slot, int button,
                                     @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.DROP, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        final StackingRule cursorRule = cursor.getStackingRule();

        ItemStack resultClicked = clicked;
        ItemStack resultCursor = cursor;


        if (slot == -999) {
            // Click outside
            if (button == 0) {
                // Left (drop all)
                var split = cursorRule.split(resultCursor, 0);
                ItemStack dropItem = split.right();
                boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultCursor = split.left();
                }
            } else if (button == 1) {
                // Right (drop 1)
                var split = cursorRule.split(resultCursor, 1);
                ItemStack dropItem = split.left();
                boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultCursor = split.right();
                }
            }

        } else if (mode == 4) {
            if (button == 0) {
                // Drop key Q (drop 1)
                var split = cursorRule.split(resultClicked, 1);
                ItemStack dropItem = split.left();
                boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultClicked = split.right();
                }
            } else if (button == 1) {
                // Ctrl + Drop key Q (drop all)
                var split = cursorRule.split(resultClicked, 0);
                ItemStack dropItem = split.right();
                boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultClicked = split.left();
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    @NotNull
    private InventoryClickResult startCondition(@NotNull InventoryClickResult clickResult, @Nullable Inventory inventory,
                                                @NotNull Player player, int slot, @NotNull ClickType clickType) {
        boolean isPlayerInventory = inventory == null;
        final int inventorySlot = isPlayerInventory ? 0 : inventory.getSize();
        isPlayerInventory = isPlayerInventory ? isPlayerInventory : slot >= inventorySlot;

        clickResult.setPlayerInventory(isPlayerInventory);

        if (isPlayerInventory && inventory != null) {
            slot = slot - inventorySlot + PlayerInventoryUtils.OFFSET;
        }

        // Call ItemStack#onInventoryClick
        {
            //clickResult.getClicked().onInventoryClick(player, clickType, slot, isPlayerInventory);
        }

        // Reset the didCloseInventory field
        // Wait for inventory conditions + events to possibly close the inventory
        player.UNSAFE_changeDidCloseInventory(false);

        // PreClickEvent
        {
            InventoryPreClickEvent inventoryPreClickEvent = new InventoryPreClickEvent(inventory, player, slot, clickType,
                    clickResult.getClicked(), clickResult.getCursor());
            player.callEvent(InventoryPreClickEvent.class, inventoryPreClickEvent);
            clickResult.setCursor(inventoryPreClickEvent.getCursorItem());
            clickResult.setClicked(inventoryPreClickEvent.getClickedItem());

            if (inventoryPreClickEvent.isCancelled()) {
                clickResult.setRefresh(true);
                clickResult.setCancel(true);
            }
        }

        // Inventory conditions
        final List<InventoryCondition> inventoryConditions = isPlayerInventory ?
                player.getInventory().getInventoryConditions() : inventory.getInventoryConditions();
        if (!inventoryConditions.isEmpty()) {

            for (InventoryCondition inventoryCondition : inventoryConditions) {
                InventoryConditionResult result = new InventoryConditionResult(clickResult.getClicked(), clickResult.getCursor());
                inventoryCondition.accept(player, slot, clickType, result);

                clickResult.setCursor(result.getCursorItem());
                clickResult.setClicked(result.getClickedItem());

                if (result.isCancel()) {
                    clickResult.setRefresh(true);
                    clickResult.setCancel(true);
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

    @NotNull
    private InventoryClickResult startCondition(@Nullable Inventory inventory, @NotNull Player player, int slot,
                                                @NotNull ClickType clickType, @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        return startCondition(clickResult, inventory, player, slot, clickType);
    }

    private void callClickEvent(@NotNull Player player, @Nullable Inventory inventory, int slot,
                                @NotNull ClickType clickType, @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(inventory, player, slot, clickType, clicked, cursor);
        player.callEvent(InventoryClickEvent.class, inventoryClickEvent);
    }

    public void clearCache(@NotNull Player player) {
        this.leftDraggingMap.remove(player);
        this.rightDraggingMap.remove(player);
    }

}
