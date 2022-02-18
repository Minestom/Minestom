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

import java.util.List;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends AbstractInventory implements EquipmentHandler {
    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_INVENTORY_SIZE = 36;

    protected final Player player;
    private ItemStack cursorItem = ItemStack.AIR;

    public PlayerInventory(@NotNull Player player) {
        super(INVENTORY_SIZE);
        this.player = player;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        // Reset cursor
        setCursorItem(ItemStack.AIR);
        // Update equipments
        this.player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
    }

    @Override
    public int getInnerSize() {
        return INNER_INVENTORY_SIZE;
    }

    @Override
    public @NotNull ItemStack getItemInMainHand() {
        return getItemStack(player.getHeldSlot());
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        safeItemInsert(player.getHeldSlot(), itemStack);
    }

    @Override
    public @NotNull ItemStack getItemInOffHand() {
        return getItemStack(OFFHAND_SLOT);
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
    }

    @Override
    public @NotNull ItemStack getHelmet() {
        return getItemStack(HELMET_SLOT);
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
    }

    @Override
    public @NotNull ItemStack getChestplate() {
        return getItemStack(CHESTPLATE_SLOT);
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
    }

    @Override
    public @NotNull ItemStack getLeggings() {
        return getItemStack(LEGGINGS_SLOT);
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
    }

    @Override
    public @NotNull ItemStack getBoots() {
        return getItemStack(BOOTS_SLOT);
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        safeItemInsert(BOOTS_SLOT, itemStack);
    }

    /**
     * Refreshes the player inventory by sending a {@link WindowItemsPacket} containing all.
     * the inventory items
     */
    @Override
    public void update() {
        this.player.getPlayerConnection().sendPacket(createWindowItemsPacket());
    }

    /**
     * Gets the item in player cursor.
     *
     * @return the cursor item
     */
    public @NotNull ItemStack getCursorItem() {
        return cursorItem;
    }

    /**
     * Changes the player cursor item.
     *
     * @param cursorItem the new cursor item
     */
    public void setCursorItem(@NotNull ItemStack cursorItem) {
        final boolean similar = this.cursorItem.isSimilar(cursorItem);
        this.cursorItem = cursorItem;

        if (!similar) {
            final SetSlotPacket setSlotPacket = SetSlotPacket.createCursorPacket(cursorItem);
            player.getPlayerConnection().sendPacket(setSlotPacket);
        }
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack) {
        EquipmentSlot equipmentSlot = null;
        if (slot == player.getHeldSlot()) {
            equipmentSlot = EquipmentSlot.MAIN_HAND;
        } else if (slot == OFFHAND_SLOT) {
            equipmentSlot = EquipmentSlot.OFF_HAND;
        } else if (slot == HELMET_SLOT) {
            equipmentSlot = EquipmentSlot.HELMET;
        } else if (slot == CHESTPLATE_SLOT) {
            equipmentSlot = EquipmentSlot.CHESTPLATE;
        } else if (slot == LEGGINGS_SLOT) {
            equipmentSlot = EquipmentSlot.LEGGINGS;
        } else if (slot == BOOTS_SLOT) {
            equipmentSlot = EquipmentSlot.BOOTS;
        }
        if (equipmentSlot != null) {
            EntityEquipEvent entityEquipEvent = new EntityEquipEvent(player, itemStack, equipmentSlot);
            EventDispatcher.call(entityEquipEvent);
            itemStack = entityEquipEvent.getEquippedItem();
        }
        this.itemStacks[slot] = itemStack;
        // Sync equipment
        if (equipmentSlot != null) {
            this.player.syncEquipment(equipmentSlot);
        }
        // Refresh slot
        sendSlotRefresh((short) convertToPacketSlot(slot), itemStack);
    }

    /**
     * Refreshes an inventory slot.
     *
     * @param slot      the packet slot,
     *                  see {@link net.minestom.server.utils.inventory.PlayerInventoryUtils#convertToPacketSlot(int)}
     * @param itemStack the item stack in the slot
     */
    protected void sendSlotRefresh(short slot, ItemStack itemStack) {
        player.getPlayerConnection().sendPacket(new SetSlotPacket((byte) 0, 0, slot, itemStack));
    }

    /**
     * Gets a {@link WindowItemsPacket} with all the items in the inventory.
     *
     * @return a {@link WindowItemsPacket} with inventory items
     */
    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < itemStacks.length; i++) {
            final int slot = convertToPacketSlot(i);
            convertedSlots[slot] = itemStacks[i];
        }
        return new WindowItemsPacket((byte) 0, 0, List.of(convertedSlots), cursorItem);
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);
        final InventoryClickResult clickResult = clickProcessor.leftClick(player, this, convertedSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(convertedSlot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        callClickEvent(player, null, convertedSlot, ClickType.LEFT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);
        final InventoryClickResult clickResult = clickProcessor.rightClick(player, this, convertedSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(convertedSlot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        callClickEvent(player, null, convertedSlot, ClickType.RIGHT_CLICK, clicked, cursor);
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
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final boolean outsideDrop = slot == -999;
        final ItemStack clicked = outsideDrop ? ItemStack.AIR : getItemStack(convertedSlot);
        final InventoryClickResult clickResult = clickProcessor.drop(player, this,
                all, convertedSlot, button, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        final ItemStack resultClicked = clickResult.getClicked();
        if (resultClicked != null && !outsideDrop) {
            setItemStack(convertedSlot, resultClicked);
        }
        setCursorItem(clickResult.getCursor());
        return true;
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);
        final boolean hotBarClick = convertSlot(slot, OFFSET) < 9;
        final int start = hotBarClick ? 9 : 0;
        final int end = hotBarClick ? getSize() - 9 : 8;
        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                this, this,
                start, end, 1,
                player, convertedSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(convertedSlot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        final ItemStack cursorItem = getCursorItem();
        if (!cursorItem.isAir()) return false;
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack heldItem = getItemStack(key);
        final ItemStack clicked = getItemStack(convertedSlot);
        final InventoryClickResult clickResult = clickProcessor.changeHeld(player, this, convertedSlot, key, clicked, heldItem);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(convertedSlot, clickResult.getClicked());
        setItemStack(key, clickResult.getCursor());
        callClickEvent(player, null, convertedSlot, ClickType.CHANGE_HELD, clicked, cursorItem);
        return true;
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = slot != -999 ? getItemStackFromPacketSlot(slot) : ItemStack.AIR;
        final InventoryClickResult clickResult = clickProcessor.dragging(player, this,
                convertPlayerInventorySlot(slot, OFFSET), button, clicked, cursor);
        if (clickResult == null || clickResult.isCancel()) {
            update();
            return false;
        }
        setCursorItem(clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);
        final InventoryClickResult clickResult = clickProcessor.doubleClick(this, this, player, convertedSlot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setCursorItem(clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    private void setItemStackFromPacketSlot(int slot, @NotNull ItemStack itemStack) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        setItemStack(convertedSlot, itemStack);
    }

    private ItemStack getItemStackFromPacketSlot(int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        return itemStacks[convertedSlot];
    }
}
