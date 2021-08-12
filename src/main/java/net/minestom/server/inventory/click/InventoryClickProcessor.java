package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@ApiStatus.Internal
public final class InventoryClickProcessor {
    // Dragging maps
    private final Map<Player, IntSet> leftDraggingMap = new ConcurrentHashMap<>();
    private final Map<Player, IntSet> rightDraggingMap = new ConcurrentHashMap<>();

    public @NotNull InventoryClickResult leftClick(@NotNull Player player, @NotNull AbstractInventory inventory,
                                                   int slot,
                                                   @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final var result = startCondition(player, inventory, slot, ClickType.LEFT_CLICK, clicked, cursor);
        if (result.isCancel()) return result;
        clicked = result.getClicked();
        cursor = result.getCursor();
        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (cursorRule.canBeStacked(cursor, clicked)) {
            // Try to stack items
            final int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);
            final int maxSize = cursorRule.getMaxSize(cursor);
            if (!clickedRule.canApply(clicked, totalAmount)) {
                // Size is too big, stack as much as possible into clicked
                result.setCursor(cursorRule.apply(cursor, totalAmount - maxSize));
                result.setClicked(clickedRule.apply(clicked, maxSize));
            } else {
                // Merge cursor item clicked
                result.setCursor(cursorRule.apply(cursor, 0));
                result.setClicked(clickedRule.apply(clicked, totalAmount));
            }
        } else {
            // Items are not compatible, swap them
            result.setCursor(clicked);
            result.setClicked(cursor);
        }
        return result;
    }

    public @NotNull InventoryClickResult rightClick(@NotNull Player player, @NotNull AbstractInventory inventory,
                                                    int slot,
                                                    @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final var result = startCondition(player, inventory, slot, ClickType.RIGHT_CLICK, clicked, cursor);
        if (result.isCancel()) return result;
        clicked = result.getClicked();
        cursor = result.getCursor();
        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (clickedRule.canBeStacked(clicked, cursor)) {
            // Items can be stacked
            final int amount = clickedRule.getAmount(clicked) + 1;
            if (!clickedRule.canApply(clicked, amount)) {
                // Size too large, stop here
                return result;
            } else {
                // Add 1 to clicked
                result.setCursor(cursorRule.apply(cursor, operand -> operand - 1));
                result.setClicked(clickedRule.apply(clicked, amount));
            }
        } else {
            // Items cannot be stacked
            if (cursor.isAir()) {
                // Take half of clicked
                final int amount = (int) Math.ceil((double) clickedRule.getAmount(clicked) / 2d);
                result.setCursor(cursorRule.apply(clicked, amount));
                result.setClicked(clickedRule.apply(clicked, operand -> operand / 2));
            } else {
                if (clicked.isAir()) {
                    // Put 1 to clicked
                    result.setCursor(cursorRule.apply(cursor, operand -> operand - 1));
                    result.setClicked(clickedRule.apply(cursor, 1));
                } else {
                    // Swap items
                    result.setCursor(clicked);
                    result.setClicked(cursor);
                }
            }
        }
        return result;
    }

    public @NotNull InventoryClickResult changeHeld(@NotNull Player player, @NotNull AbstractInventory inventory,
                                                    int slot, int key,
                                                    @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        // Verify the clicked item
        InventoryClickResult clickResult = startCondition(player, inventory, slot, ClickType.CHANGE_HELD, clicked, cursor);
        if (clickResult.isCancel()) return clickResult;
        // Verify the destination (held bar)
        clickResult = startCondition(player, player.getInventory(), key, ClickType.CHANGE_HELD, clicked, cursor);
        if (clickResult.isCancel()) return clickResult;
        // Swap items
        clickResult.setClicked(cursor);
        clickResult.setCursor(clicked);
        return clickResult;
    }

    public @Nullable InventoryClickResult shiftClick(@NotNull AbstractInventory inventory, @NotNull AbstractInventory targetInventory,
                                                     int start, int end, int step,
                                                     @NotNull Player player, int slot,
                                                     @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(player, inventory, slot, ClickType.START_SHIFT_CLICK, clicked, cursor);
        if (clickResult.isCancel()) return clickResult;
        if (clicked.isAir()) return null;
        final var pair = TransactionType.ADD.process(targetInventory, clicked, (index, itemStack) -> {
            if (inventory == targetInventory && index == slot)
                return false; // Prevent item lose/duplication
            InventoryClickResult result = startCondition(player, targetInventory, index, ClickType.SHIFT_CLICK, itemStack, cursor);
            if (result.isCancel()) {
                clickResult.setRefresh(true);
                return false;
            }
            return true;
        }, start, end, step);
        ItemStack itemResult = TransactionOption.ALL.fill(targetInventory, pair.left(), pair.right());
        clickResult.setClicked(itemResult);
        return clickResult;
    }

    public @Nullable InventoryClickResult dragging(@NotNull Player player, @NotNull AbstractInventory inventory,
                                                   int slot, int button,
                                                   @NotNull ItemStack clicked, @NotNull ItemStack cursor,
                                                   @NotNull Int2ObjectFunction<ItemStack> itemGetter,
                                                   @NotNull BiConsumer<Integer, ItemStack> itemSetter) {
        InventoryClickResult clickResult = null;

        final StackingRule stackingRule = cursor.getStackingRule();

        if (slot == -999) {
            // Start or end left/right drag
            if (button == 0) {
                // Start left
                clickResult = startCondition(player, inventory, slot, ClickType.START_LEFT_DRAGGING, clicked, cursor);
                if (!clickResult.isCancel())
                    this.leftDraggingMap.put(player, new IntOpenHashSet());
            } else if (button == 4) {
                // Start right
                clickResult = startCondition(player, inventory, slot, ClickType.START_RIGHT_DRAGGING, clicked, cursor);
                if (!clickResult.isCancel())
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

                boolean cancel = false;

                for (int s : slots) {
                    ItemStack slotItem = itemGetter.apply(s);
                    clickResult = startCondition(player, inventory, s, ClickType.LEFT_DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel()) {
                        cancel = true;
                        break;
                    }
                }

                cancel |= startCondition(player, inventory, slot, ClickType.END_LEFT_DRAGGING, clicked, cursor).isCancel();

                // Should be size of each defined slot (if not full)
                final int slotSize = (int) ((float) cursorAmount / (float) slotCount);
                int finalCursorAmount = cursorAmount;

                if (!cancel) {
                    for (int s : slots) {
                        ItemStack slotItem = itemGetter.apply(s);
                        StackingRule slotItemRule = slotItem.getStackingRule();

                        final int maxSize = stackingRule.getMaxSize(cursor);
                        if (stackingRule.canBeStacked(cursor, slotItem)) {
                            final int amount = slotItemRule.getAmount(slotItem);
                            if (stackingRule.canApply(slotItem, amount + slotSize)) {
                                slotItem = stackingRule.apply(slotItem, a -> a + slotSize);
                                finalCursorAmount -= slotSize;
                            } else {
                                final int removedAmount = maxSize - amount;
                                slotItem = stackingRule.apply(slotItem, maxSize);
                                finalCursorAmount -= removedAmount;
                            }
                        } else if (slotItem.isAir()) {
                            slotItem = stackingRule.apply(cursor, slotSize);
                            finalCursorAmount -= slotSize;
                        }
                        itemSetter.accept(s, slotItem);

                        callClickEvent(player, inventory, s, ClickType.LEFT_DRAGGING, slotItem, cursor);
                    }

                    // If no slots were dragged over, no need to apply any kind of stacking rules
                    if (clickResult != null) {
                        cursor = stackingRule.apply(cursor, finalCursorAmount);
                        clickResult.setCursor(cursor);
                    }
                }

                leftDraggingMap.remove(player);
            } else if (button == 6) {
                // End right
                if (!rightDraggingMap.containsKey(player))
                    return null;
                final IntSet slots = rightDraggingMap.get(player);
                final int size = slots.size();
                int cursorAmount = stackingRule.getAmount(cursor);
                if (size > cursorAmount)
                    return null;

                boolean cancel = false;

                for (int s : slots) {
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(player, inventory, s, ClickType.RIGHT_DRAGGING, slotItem, cursor);

                    if (clickResult.isCancel()) {
                        cancel = true;

                        break;
                    }
                }

                cancel |= startCondition(player, inventory, slot, ClickType.END_RIGHT_DRAGGING, clicked, cursor).isCancel();

                if (!cancel) {
                    for (int s : slots) {
                        ItemStack draggedItem = cursor;
                        ItemStack slotItem = itemGetter.apply(s);

                        StackingRule slotItemRule = slotItem.getStackingRule();
                        if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                            final int amount = slotItemRule.getAmount(slotItem) + 1;
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

                        callClickEvent(player, inventory, s, ClickType.RIGHT_DRAGGING, draggedItem, cursor);
                    }

                    // If no slots were dragged over, no need to apply any kind of stacking rules
                    if (clickResult != null) {
                        cursor = stackingRule.apply(cursor, cursorAmount);
                        clickResult.setCursor(cursor);
                    }
                }

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

    public @Nullable InventoryClickResult doubleClick(@NotNull AbstractInventory clickedInventory, @NotNull AbstractInventory inventory, @NotNull Player player, int slot,
                                                      @NotNull final ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(player, inventory, slot, ClickType.START_DOUBLE_CLICK, ItemStack.AIR, cursor);
        if (clickResult.isCancel()) return clickResult;
        if (cursor.isAir()) return null;

        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return clickResult;
        }
        final BiFunction<AbstractInventory, ItemStack, ItemStack> func = (inv, rest) -> {
            var pair = TransactionType.TAKE.process(inv, rest, (index, itemStack) -> {
                if (index == slot) // Prevent item lose/duplication
                    return false;
                final InventoryClickResult result = startCondition(player, inventory, index, ClickType.DOUBLE_CLICK, itemStack, cursor);
                return !result.isCancel();
            });
            var itemResult = pair.left();
            var map = pair.right();
            return TransactionOption.ALL.fill(inv, itemResult, map);
        };

        ItemStack remain = cursorRule.apply(cursor, remainingAmount);
        final var playerInventory = player.getInventory();
        // Retrieve remain
        if (Objects.equals(clickedInventory, inventory)) {
            // Clicked inside inventory
            remain = func.apply(inventory, remain);
            if (!remain.isAir()) {
                remain = func.apply(playerInventory, remain);
            }
        } else if (clickedInventory == playerInventory) {
            // Clicked inside player inventory, but with another inventory open
            remain = func.apply(playerInventory, remain);
            if (!remain.isAir()) {
                remain = func.apply(inventory, remain);
            }
        } else {
            // Clicked inside player inventory
            remain = func.apply(playerInventory, remain);
        }

        // Update cursor based on the remaining
        if (remain.isAir()) {
            // Item has been filled
            clickResult.setCursor(cursorRule.apply(cursor, maxSize));
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remain);
            clickResult.setCursor(cursorRule.apply(cursor, amount + tookAmount));
        }
        return clickResult;
    }

    public @NotNull InventoryClickResult drop(@NotNull Player player, @NotNull AbstractInventory inventory,
                                              boolean all, int slot, int button,
                                              @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(player, inventory, slot, ClickType.DROP, clicked, cursor);
        if (clickResult.isCancel()) return clickResult;

        final StackingRule clickedRule = clicked.getStackingRule();
        final StackingRule cursorRule = cursor.getStackingRule();

        ItemStack resultClicked = clicked;
        ItemStack resultCursor = cursor;

        if (slot == -999) {
            // Click outside
            if (button == 0) {
                // Left (drop all)
                final int amount = cursorRule.getAmount(resultCursor);
                final ItemStack dropItem = cursorRule.apply(resultCursor, amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultCursor = cursorRule.apply(resultCursor, 0);
                }
            } else if (button == 1) {
                // Right (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultCursor, 1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = cursorRule.getAmount(resultCursor);
                    final int newAmount = amount - 1;
                    resultCursor = cursorRule.apply(resultCursor, newAmount);
                }
            }

        } else if (!all) {
            if (button == 0) {
                // Drop key Q (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultClicked, 1);
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
                final ItemStack dropItem = clickedRule.apply(resultClicked, amount);
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

    private @NotNull InventoryClickResult startCondition(@NotNull Player player,
                                                         @NotNull AbstractInventory inventory,
                                                         int slot, @NotNull ClickType clickType,
                                                         @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        final Inventory eventInventory = inventory instanceof Inventory ? (Inventory) inventory : null;
        clickResult.setPlayerInventory(eventInventory == null);

        // Reset the didCloseInventory field
        // Wait for inventory conditions + events to possibly close the inventory
        player.UNSAFE_changeDidCloseInventory(false);
        // InventoryPreClickEvent
        {
            InventoryPreClickEvent inventoryPreClickEvent = new InventoryPreClickEvent(eventInventory, player, slot, clickType,
                    clickResult.getClicked(), clickResult.getCursor());
            EventDispatcher.call(inventoryPreClickEvent);
            clickResult.setCursor(inventoryPreClickEvent.getCursorItem());
            clickResult.setClicked(inventoryPreClickEvent.getClickedItem());
            if (inventoryPreClickEvent.isCancelled()) {
                clickResult.setRefresh(true);
                clickResult.setCancel(true);
            }
        }
        // Inventory conditions
        {
            final var inventoryConditions = inventory.getInventoryConditions();
            if (!inventoryConditions.isEmpty()) {
                for (InventoryCondition inventoryCondition : inventoryConditions) {
                    var result = new InventoryConditionResult(clickResult.getClicked(), clickResult.getCursor());
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
        }
        return clickResult;
    }

    private void callClickEvent(@NotNull Player player, @Nullable AbstractInventory inventory, int slot,
                                @NotNull ClickType clickType, @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final Inventory eventInventory = inventory instanceof Inventory ? (Inventory) inventory : null;
        EventDispatcher.call(new InventoryClickEvent(eventInventory, player, slot, clickType, clicked, cursor));
    }

    public void clearCache(@NotNull Player player) {
        this.leftDraggingMap.remove(player);
        this.rightDraggingMap.remove(player);
    }
}
