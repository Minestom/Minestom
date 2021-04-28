package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.entity.Player;
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
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@ApiStatus.Internal
public class InventoryClickProcessor {

    // Dragging maps
    private final Map<Player, IntSet> leftDraggingMap = new ConcurrentHashMap<>();
    private final Map<Player, IntSet> rightDraggingMap = new ConcurrentHashMap<>();

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
            final int maxSize = cursorRule.getMaxSize(cursor);

            if (!clickedRule.canApply(clicked, totalAmount)) {
                resultCursor = cursorRule.apply(cursor, totalAmount - maxSize);
                resultClicked = clickedRule.apply(clicked, maxSize);
            } else {
                resultCursor = cursorRule.apply(cursor, 0);
                resultClicked = clickedRule.apply(clicked, totalAmount);
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
                resultCursor = cursorRule.apply(cursor, operand -> operand - 1);
                resultClicked = clickedRule.apply(clicked, amount);
            }
        } else {
            if (cursor.isAir()) {
                final int amount = (int) Math.ceil((double) clickedRule.getAmount(clicked) / 2d);
                resultCursor = cursorRule.apply(clicked, amount);
                resultClicked = clickedRule.apply(clicked, operand -> operand / 2);
            } else {
                if (clicked.isAir()) {
                    resultCursor = cursorRule.apply(cursor, operand -> operand - 1);
                    resultClicked = clickedRule.apply(cursor, 1);
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

    public @Nullable InventoryClickResult shiftClick(AbstractInventory targetInventory, @Nullable Inventory inventory, @NotNull Player player, int slot,
                                                     @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.START_SHIFT_CLICK, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (clicked.isAir())
            return null;

        var pair = TransactionType.ADD.process(targetInventory, clicked, (index, itemStack) -> {
            InventoryClickResult result = startCondition(targetInventory, player, index, ClickType.SHIFT_CLICK, itemStack, cursor);
            if(result.isCancel()){
                clickResult.setRefresh(true);
                return false;
            }
            return true;
        });

        ItemStack itemResult = TransactionOption.ALL.fill(targetInventory, pair.left(), pair.right());
        clickResult.setClicked(itemResult);
        return clickResult;
    }

    public @Nullable InventoryClickResult shiftClick(@NotNull AbstractInventory targetInventory, int start, int end, int step, @NotNull Player player, int slot,
                                                     @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(null, player, slot, ClickType.START_SHIFT_CLICK, clicked, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (clicked.isAir())
            return null;

        var pair = TransactionType.ADD.process(targetInventory, clicked, (index, itemStack) -> {
            if (index == slot) // Prevent item lose/duplication
                return false;
            InventoryClickResult result = startCondition(targetInventory, player, index, ClickType.SHIFT_CLICK, itemStack, cursor);
            if(result.isCancel()){
                clickResult.setRefresh(true);
                return false;
            }
            return true;
        }, start, end, step);

        ItemStack itemResult = TransactionOption.ALL.fill(targetInventory, pair.left(), pair.right());
        clickResult.setClicked(itemResult);
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
                int finalCursorAmount = cursorAmount;

                for (int s : slots) {
                    ItemStack slotItem = itemGetter.apply(s);

                    clickResult = startCondition(inventory, player, s, ClickType.DRAGGING, slotItem, cursor);
                    if (clickResult.isCancel())
                        break;
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

                    callClickEvent(player, inventory, s, ClickType.DRAGGING, slotItem, cursor);
                }
                cursor = stackingRule.apply(cursor, finalCursorAmount);
                clickResult.setCursor(cursor);

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

    @Nullable
    public InventoryClickResult doubleClick(@NotNull AbstractInventory clickedInventory, @Nullable Inventory inventory, @NotNull Player player, int slot,
                                            @NotNull final ItemStack cursor) {
        InventoryClickResult clickResult = startCondition(inventory, player, slot, ClickType.START_DOUBLE_CLICK, ItemStack.AIR, cursor);

        if (clickResult.isCancel()) {
            return clickResult;
        }

        if (cursor.isAir())
            return null;

        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;

        if (remainingAmount == 0) {
            // Item is already full
            return clickResult;
        }

        ItemStack remain = cursorRule.apply(cursor, remainingAmount);

        BiFunction<AbstractInventory, ItemStack, ItemStack> func = (inv, rest) -> {
            var pair = TransactionType.TAKE.process(inv, rest, (index, itemStack) -> {
                if (index == slot) // Prevent item lose/duplication
                    return false;
                InventoryClickResult result = startCondition(inventory, player, index, ClickType.DOUBLE_CLICK, itemStack, cursor);
                return !result.isCancel();
            });
            var itemResult = pair.left();
            var map = pair.right();
            return TransactionOption.ALL.fill(inv, itemResult, map);
        };

        var playerInventory = player.getInventory();

        if (Objects.equals(clickedInventory, inventory)) {
            // Clicked inside inventory
            remain = func.apply(inventory, remain);
            if (!remain.isAir()) {
                remain = func.apply(playerInventory, remain);
            }
        } else if (inventory != null && clickedInventory == playerInventory) {
            // Clicked inside player inventory, but with another inventory open
            remain = func.apply(playerInventory, remain);
            if (!remain.isAir()) {
                remain = func.apply(inventory, remain);
            }
        } else {
            // Clicked inside player inventory
            remain = func.apply(playerInventory, remain);
        }

        ItemStack resultCursor;
        if (remain.isAir()) {
            // Item has been filled
            resultCursor = cursorRule.apply(cursor, maxSize);
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remain);
            resultCursor = cursorRule.apply(cursor, amount + tookAmount);
        }

        clickResult.setCursor(resultCursor);
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

        } else if (mode == 4) {
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

    @NotNull
    private InventoryClickResult startCondition(@Nullable AbstractInventory inventory, @NotNull Player player, int slot,
                                                @NotNull ClickType clickType, @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        return startCondition(inventory instanceof Inventory ? (Inventory) inventory : null,
                player, slot, clickType, clicked, cursor);
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
