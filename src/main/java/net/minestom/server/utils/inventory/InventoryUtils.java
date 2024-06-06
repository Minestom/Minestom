package net.minestom.server.utils.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.*;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ButtonType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InventoryUtils {

    public static boolean callOffhandSwap(@NotNull Player player, @NotNull Inventory inventory, int slot) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack offhand = playerInventory.getItemStack(PlayerInventoryUtils.OFF_HAND_SLOT);
        ItemStack swapped = inventory.getItemStack(slot);

        OffhandSwapItemEvent event = new OffhandSwapItemEvent(player, playerInventory, inventory, slot, offhand, swapped);

        EventDispatcher.call(event);
        if (event.isCancelled()) return false;

        // Apply changes
        playerInventory.setItemStack(PlayerInventoryUtils.OFF_HAND_SLOT, event.getOffHandItem());
        inventory.setItemStack(slot, event.getSwappedItem());
        return true;
    }

    public static boolean callHotbarSwap(@NotNull Player player, @NotNull Inventory inventory, int clickedSlot, int hotbarSlot) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack hotbar = playerInventory.getItemStack(hotbarSlot);
        ItemStack swapped = inventory.getItemStack(clickedSlot);

        HotbarSwapItemEvent event = new HotbarSwapItemEvent(player, playerInventory, inventory, clickedSlot, hotbarSlot, hotbar, swapped);

        EventDispatcher.call(event);
        if (event.isCancelled()) return false;

        // Apply changes
        playerInventory.setItemStack(hotbarSlot, event.getHotbarItem());
        inventory.setItemStack(clickedSlot, event.getSwappedItem());
        return true;
    }

    public static boolean callDrag(@NotNull Player player, @NotNull Inventory inventory, @NotNull ButtonType initialButtonType, @NotNull List<Integer> slots) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack distributedItem = playerInventory.getCursorItem();

        // TODO: Implement
        // (code already exists in ClickProcessors (just copy over)

        return false;
    }

    public static boolean callDropCursor(@NotNull Player player, @NotNull Inventory inventory, @NotNull ButtonType initialButtonType) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack item = playerInventory.getCursorItem();

        int amount = switch (initialButtonType) {
            case LEFT -> item.amount();
            case MIDDLE -> 0;
            case RIGHT -> 1;
        };

        DropCursorEvent event = new DropCursorEvent(player, playerInventory, inventory, initialButtonType, item.withAmount(amount));

        EventDispatcher.call(event);
        if (event.isCancelled()) return false;

        ItemStack dropped = event.getDroppedItem();

        if (!player.dropItem(dropped)) return false;

        playerInventory.setCursorItem(item.withAmount(a -> a - dropped.amount()));

        return true;
    }

    public static boolean callDropSlot(@NotNull Player player, @NotNull Inventory inventory, boolean all, int slot) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack item = inventory.getItemStack(slot);

        int amount = all ? item.amount() : 1;

        DropSlotEvent event = new DropSlotEvent(player, playerInventory, inventory, all, slot, item.withAmount(amount));

        EventDispatcher.call(event);
        if (event.isCancelled()) return false;

        ItemStack dropped = event.getDroppedItem();

        if (!player.dropItem(dropped)) return false;

        inventory.setItemStack(slot, item.withAmount(a -> a - dropped.amount()));

        return true;
    }

    public static boolean callClick(@NotNull Player player, @NotNull Inventory inventory, @NotNull ClickType type, int slot) {
        // TODO: Implement
        // (code already exists in ClickProcessors (just copy over)

        return false;
    }

    public static boolean callCreativeAction(@NotNull Player player, @NotNull Inventory inventory, int slot, @NotNull ItemStack item) {
        PlayerInventory playerInventory = player.getInventory();

        CreativeInventoryActionEvent event = new CreativeInventoryActionEvent(player, playerInventory, inventory, slot, item);

        EventDispatcher.call(event);
        if (event.isCancelled()) return false;

        slot = event.getSlot();

        if (slot == -1) {
            return player.dropItem(item);
        } else {
            inventory.setItemStack(slot, item);
            return true;
        }
    }



}
