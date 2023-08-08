package net.minestom.server.inventory;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends AbstractInventory {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_INVENTORY_SIZE = 36;

    public static final int HELMET_SLOT = 5;
    public static final int CHESTPLATE_SLOT = 6;
    public static final int LEGGINGS_SLOT = 7;
    public static final int BOOTS_SLOT = 8;
    public static final int OFFHAND_SLOT = 45;

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public synchronized void clear() {
        super.clear();

        // Update equipment
        for (var player : getViewers()) {
            player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
        }
    }

    @Override
    public int getInnerSize() {
        return INNER_INVENTORY_SIZE;
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack, boolean sendPacket) {
        this.itemStacks[slot] = itemStack;

        for (var player : getViewers()) {
            final EquipmentSlot equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());

            if (equipmentSlot != null) {
                EntityEquipEvent entityEquipEvent = new EntityEquipEvent(player, itemStack, equipmentSlot);
                EventDispatcher.call(entityEquipEvent);
                itemStack = entityEquipEvent.getEquippedItem();

                if (sendPacket) {
                    player.syncEquipment(equipmentSlot);
                }
            }

        }

        if (sendPacket) {
            sendSlotRefresh((short) slot, itemStack);
        }
    }

    private int getSlotIndex(@NotNull EquipmentSlot slot, int heldSlot) {
        return switch (slot) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> slot.armorSlot();
            case OFF_HAND -> OFFHAND_SLOT;
            case MAIN_HAND -> heldSlot;
        };
    }

    private @Nullable EquipmentSlot fromSlotIndex(int slot, int heldSlot) {
        return switch (slot) {
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case OFFHAND_SLOT -> EquipmentSlot.OFF_HAND;
            default -> slot == heldSlot ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot, int heldSlot) {
        return getItemStack(getSlotIndex(slot, heldSlot));
    }

    public void setEquipment(@NotNull EquipmentSlot slot, int heldSlot, @NotNull ItemStack newValue) {
        setItemStack(getSlotIndex(slot, heldSlot), newValue);
    }

    @Override
    public byte getWindowId() {
        return 0;
    }

    /**
     * Refreshes an inventory slot.
     *
     * @param slot      the packet slot
     * @param itemStack the item stack in the slot
     */
    protected void sendSlotRefresh(short slot, ItemStack itemStack) {
        SetSlotPacket defaultPacket = new SetSlotPacket((byte) 0, 0, slot, itemStack);

        for (Player viewer : getViewers()) {
            var openInventory = player.getOpenInventory();
            if (openInventory != null && slot >= OFFSET && slot < OFFSET + INNER_INVENTORY_SIZE) {
                this.player.sendPacket(new SetSlotPacket(openInventory.getWindowId(), 0, (short) (slot + openInventory.getSize() - OFFSET), itemStack));
            } else if (openInventory == null || slot == OFFHAND_SLOT) {
                this.player.sendPacket(defaultPacket);
            }
        }
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem(player);
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.leftClick(player, this, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(player, clickResult.getCursor());
        callClickEvent(player, null, slot, ClickType.LEFT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem(player);
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.rightClick(player, this, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(player, clickResult.getCursor());
        callClickEvent(player, null, slot, ClickType.RIGHT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        update();
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, boolean all, int slot, int button) {
        final ItemStack cursor = getCursorItem(player);
        final boolean outsideDrop = slot == -999;
        final ItemStack clicked = outsideDrop ? ItemStack.AIR : getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.drop(player, this,
                all, slot, button, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        final ItemStack resultClicked = clickResult.getClicked();
        if (resultClicked != null && !outsideDrop) {
            setItemStack(slot, resultClicked);
        }
        setCursorItem(player, clickResult.getCursor());
        return true;
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem(player);
        final ItemStack clicked = getItemStack(slot);
        final boolean hotBarClick = slot >= 36 && slot <= 44;
        final int start = hotBarClick ? 9 : 0;
        final int end = hotBarClick ? getSize() - 9 : 8;
        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                this, this,
                start, end, 1,
                player, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(player, clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final int convertedKey = key == 40 ? OFFHAND_SLOT : key;
        final ItemStack cursorItem = getCursorItem(player);
        if (!cursorItem.isAir()) return false;
        final ItemStack heldItem = getItemStack(convertedKey);
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.changeHeld(player, this, slot, convertedKey, clicked, heldItem);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setItemStack(convertedKey, clickResult.getCursor());
        callClickEvent(player, null, slot, ClickType.CHANGE_HELD, clicked, cursorItem);
        return true;
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final ItemStack cursor = getCursorItem(player);
        final ItemStack clicked = slot != -999 ? getItemStackFromPacketSlot(slot) : ItemStack.AIR;
        final InventoryClickResult clickResult = clickProcessor.dragging(player, this,
                slot, button, clicked, cursor);
        if (clickResult == null || clickResult.isCancel()) {
            update();
            return false;
        }
        setCursorItem(player, clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem(player);
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.doubleClick(this, this, player, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setCursorItem(player, clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    private void setItemStackFromPacketSlot(int slot, @NotNull ItemStack itemStack) {
        setItemStack(slot, itemStack);
    }

    private ItemStack getItemStackFromPacketSlot(int slot) {
        return itemStacks[slot];
    }
}
