package net.minestom.server.inventory.click;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@ApiStatus.Internal
public final class InventoryClickProcessor {

    public InventoryClickResult leftClick(ItemStack clicked, ItemStack cursor) {
        if (cursor.isSimilar(clicked)) {
            // Try to stack items
            final int totalAmount = cursor.amount() + clicked.amount();
            final int maxSize = cursor.maxStackSize();
            if (!MathUtils.isBetween(totalAmount, 0, clicked.maxStackSize())) {
                // Size is too big, stack as much as possible into clicked
                cursor = cursor.withAmount(totalAmount - maxSize);
                clicked = clicked.withAmount(maxSize);
            } else {
                // Merge cursor item clicked
                cursor = ItemStack.AIR;
                clicked = clicked.withAmount(totalAmount);
            }
        } else {
            // Items are not compatible, swap them
            var temp = clicked;

            clicked = cursor;
            cursor = temp;
        }
        return new InventoryClickResult(clicked, cursor);
    }

    public InventoryClickResult rightClick(ItemStack clicked, ItemStack cursor) {
        final var result = new InventoryClickResult(clicked, cursor);

        if (clicked.isSimilar(cursor)) {
            // Items can be stacked
            final int amount = clicked.amount() + 1;
            if (!MathUtils.isBetween(amount, 0, clicked.maxStackSize())) {
                // Size too large, stop here
                return result;
            } else {
                // Add 1 to clicked
                result.setCursor(cursor.withAmount(operand -> operand - 1));
                result.setClicked(clicked.withAmount(amount));
            }
        } else {
            // Items cannot be stacked
            if (cursor.isAir()) {
                // Take half of clicked
                final int amount = (int) Math.ceil((double) clicked.amount() / 2d);
                result.setCursor(clicked.withAmount(amount));
                result.setClicked(clicked.withAmount(operand -> operand / 2));
            } else {
                if (clicked.isAir()) {
                    // Put 1 to clicked
                    result.setCursor(cursor.withAmount(operand -> operand - 1));
                    result.setClicked(cursor.withAmount(1));
                } else {
                    // Swap items
                    result.setCursor(clicked);
                    result.setClicked(cursor);
                }
            }
        }
        return result;
    }

    public InventoryClickResult changeHeld(ItemStack clicked, ItemStack cursor) {
        return new InventoryClickResult(cursor, clicked); // Swap items
    }

    public InventoryClickResult shiftClick(AbstractInventory inventory, AbstractInventory targetInventory,
                                                    int start, int end, int step,
                                                    Player player, int slot,
                                                    ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        if (clicked.isAir()) return clickResult.cancelled();

        final boolean craftingGridClick = slot >= 36 && slot <= 40;

        // Handle armor and off-hand equippables
        if (inventory instanceof PlayerInventory && targetInventory instanceof PlayerInventory) {
            Equippable equippableComponent = clicked.get(DataComponents.EQUIPPABLE);
            if (equippableComponent != null) {
                final EquipmentSlot equipmentSlot = equippableComponent.slot();
                RegistryTag<EntityType> allowed = equippableComponent.allowedEntities();
                if ((allowed == null || allowed.contains(EntityType.PLAYER))
                        && (equipmentSlot.isArmor() || equipmentSlot == EquipmentSlot.OFF_HAND)
                        && !craftingGridClick) {
                    // Shift-click equip
                    final ItemStack currentItem = player.getEquipment(equipmentSlot);
                    if (currentItem.isAir()) {
                        player.setEquipment(equipmentSlot, clicked);
                        return new InventoryClickResult(ItemStack.AIR, cursor);
                    }
                }
            }
        }

        clickResult.setCancel(true);
        final var pair = TransactionType.ADD.process(targetInventory, clicked, (index, itemStack) -> {
            if (inventory == targetInventory && index == slot) {
                return false; // Prevent item lose/duplication
            }

            clickResult.setCancel(false);
            return true;
        }, start, end, step);

        final ItemStack itemResult = pair.left();
        final Map<Integer, ItemStack> itemChangesMap = pair.right();
        itemChangesMap.forEach((Integer s, ItemStack itemStack) -> {
            targetInventory.setItemStack(s, itemStack);
            callClickEvent(player, targetInventory, s, ClickType.SHIFT_CLICK, itemStack, cursor);
        });

        clickResult.setClicked(itemResult);
        return clickResult;
    }

    public @Nullable ItemStack dragging(Player player, AbstractInventory inventory,
                                        List<Integer> slots, int button, ItemStack cursor) {
        // Drag instruction
        if (button == 2) {
            // End left
            final int slotCount = slots.size();
            final int cursorAmount = cursor.amount();
            if (slotCount > cursorAmount) return null;

            // Should be size of each defined slot (if not full)
            final int slotSize = (int) ((float) cursorAmount / (float) slotCount);
            // Place all waiting drag action
            int finalCursorAmount = cursorAmount;
            for (int slot : slots) {
                final boolean isInWindow = slot < inventory.getSize();
                final var inv = isInWindow ? inventory : player.getInventory();
                final int s = isInWindow ? slot : slot - inventory.getSize();

                ItemStack slotItem = inv.getItemStack(s);
                final int amount = slotItem.amount();
                if (cursor.isSimilar(slotItem)) {
                    if (MathUtils.isBetween(amount + slotSize, 0, slotItem.maxStackSize())) {
                        // Append divided amount to slot
                        slotItem = slotItem.withAmount(a -> a + slotSize);
                        finalCursorAmount -= slotSize;
                    } else {
                        // Amount too big, fill as much as possible
                        final int maxSize = cursor.maxStackSize();
                        final int removedAmount = maxSize - amount;
                        slotItem = slotItem.withAmount(maxSize);
                        finalCursorAmount -= removedAmount;
                    }
                } else if (slotItem.isAir()) {
                    // Slot is empty, add divided amount
                    slotItem = cursor.withAmount(slotSize);
                    finalCursorAmount -= slotSize;
                }
                inv.setItemStack(s, slotItem);
                callClickEvent(player, inv, s, ClickType.LEFT_DRAGGING, slotItem, cursor);
            }
            // Update the cursor
            cursor = cursor.withAmount(finalCursorAmount);
        } else if (button == 6) {
            // End right
            int cursorAmount = cursor.amount();
            if (slots.size() > cursorAmount) return null;
            // Place all waiting drag action
            int finalCursorAmount = cursorAmount;
            for (int slot : slots) {
                final boolean isInWindow = slot < inventory.getSize();
                final var inv = isInWindow ? inventory : player.getInventory();
                final int s = isInWindow ? slot : slot - inventory.getSize();

                ItemStack slotItem = inv.getItemStack(s);
                if (cursor.isSimilar(slotItem)) {
                    // Compatible item in the slot, increment by 1
                    final int amount = slotItem.amount() + 1;
                    if (MathUtils.isBetween(amount, 0, slotItem.maxStackSize())) {
                        slotItem = slotItem.withAmount(amount);
                        finalCursorAmount -= 1;
                    }
                } else if (slotItem.isAir()) {
                    // No item at the slot, place one
                    slotItem = cursor.withAmount(1);
                    finalCursorAmount -= 1;
                }
                inv.setItemStack(s, slotItem);
                callClickEvent(player, inv, s, ClickType.RIGHT_DRAGGING, slotItem, cursor);
            }
            // Update the cursor
            cursor = cursor.withAmount(finalCursorAmount);
        }

        return cursor;
    }

    public InventoryClickResult doubleClick(AbstractInventory clickedInventory, AbstractInventory inventory, Player player, int slot,
                                                     ItemStack clicked, ItemStack cursor) {
        InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        if (cursor.isAir()) return clickResult.cancelled();

        final int amount = cursor.amount();
        final int maxSize = cursor.maxStackSize();
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return clickResult;
        }
        final BiFunction<AbstractInventory, ItemStack, ItemStack> func = (inv, rest) -> {
            var pair = TransactionType.TAKE.process(inv, rest, (index, itemStack) -> {
                // Prevent item loss/duplication
                return index != slot || clickedInventory != inv;
            });
            final ItemStack itemResult = pair.left();
            var itemChangesMap = pair.right();
            itemChangesMap.forEach((Integer s, ItemStack itemStack) -> {
                inv.setItemStack(s, itemStack);
                callClickEvent(player, inv, s, ClickType.DOUBLE_CLICK, itemStack, cursor);
            });
            return itemResult;
        };

        ItemStack remain = cursor.withAmount(remainingAmount);
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
            clickResult.setCursor(cursor.withAmount(maxSize));
        } else {
            final int tookAmount = remainingAmount - remain.amount();
            clickResult.setCursor(cursor.withAmount(amount + tookAmount));
        }
        return clickResult;
    }

    public InventoryClickResult drop(Player player,
                                              boolean all, int slot, ItemStack clicked, ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);

        if (slot == -999) {
            // Click outside
            if (all) {
                // Left (drop all)
                final int amount = cursor.amount();
                final ItemStack dropItem = cursor.withAmount(amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    cursor = ItemStack.AIR;
                }
            } else {
                // Right (drop 1)
                final ItemStack dropItem = cursor.withAmount(1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = cursor.amount();
                    final int newAmount = amount - 1;
                    cursor = cursor.withAmount(newAmount);
                }
            }

        } else {
            if (all) {
                // Ctrl + Drop key Q (drop all)
                final int amount = clicked.amount();
                final ItemStack dropItem = clicked.withAmount(amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    clicked = ItemStack.AIR;
                }
            } else {
                // Drop key Q (drop 1)
                final ItemStack dropItem = clicked.withAmount(1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = clicked.amount();
                    final int newAmount = amount - 1;
                    clicked = clicked.withAmount(newAmount);
                }
            }
        }

        clickResult.setClicked(clicked);
        clickResult.setCursor(cursor);

        return clickResult;
    }

    private void callClickEvent(Player player, AbstractInventory inventory, int slot,
                                ClickType clickType, ItemStack clicked, ItemStack cursor) {
        EventDispatcher.call(new InventoryClickEvent(inventory, player, slot, clickType, clicked, cursor));
    }
}