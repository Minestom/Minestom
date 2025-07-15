package net.minestom.server.inventory;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends AbstractInventory {
    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_INVENTORY_SIZE = 36;

    private ItemStack cursorItem = ItemStack.AIR;

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public synchronized void clear() {
        cursorItem = ItemStack.AIR;
        super.clear();

        // Update equipments
        viewers.forEach(viewer -> viewer.sendPacketToViewersAndSelf(viewer.getEquipmentsPacket()));
    }

    @Override
    public int getInnerSize() {
        return INNER_INVENTORY_SIZE;
    }

    @Override
    public byte getWindowId() {
        return 0;
    }

    private int getSlotId(EquipmentSlot slot, byte heldSlot) {
        return switch (slot) {
            case MAIN_HAND -> heldSlot;
            case OFF_HAND -> OFFHAND_SLOT;
            default -> slot.armorSlot();
        };
    }

    private @Nullable EquipmentSlot getEquipmentSlot(int slot, byte heldSlot) {
        return switch (slot) {
            case OFFHAND_SLOT -> EquipmentSlot.OFF_HAND;
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            default -> slot == heldSlot ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    public ItemStack getEquipment(EquipmentSlot slot, byte heldSlot) {
        final int slotId = getSlotId(slot, heldSlot);
        if (slotId < 0) return ItemStack.AIR;
        return getItemStack(slotId);
    }

    public void setEquipment(EquipmentSlot slot, byte heldSlot, ItemStack itemStack) {
        final int slotId = getSlotId(slot, heldSlot);
        if (slotId < 0) Check.fail("PlayerInventory does not support " + slot + " equipment");

        setItemStack(slotId, itemStack);
    }

    @Override
    public void update(Player player) {
        player.sendPacket(createWindowItemsPacket());
    }

    /**
     * Gets the item in player cursor.
     *
     * @return the cursor item
     */
    public ItemStack getCursorItem() {
        return cursorItem;
    }

    /**
     * Changes the player cursor item.
     *
     * @param cursorItem the new cursor item
     */
    public void setCursorItem(ItemStack cursorItem) {
        setCursorItem(cursorItem, true);
    }

    /**
     * Changes the player cursor item.
     *
     * @param cursorItem the new cursor item
     * @param sendPacket true to send the update packet to the client, false otherwise
     */
    public void setCursorItem(ItemStack cursorItem, boolean sendPacket) {
        if (this.cursorItem.equals(cursorItem)) return;
        this.cursorItem = cursorItem;
        if (sendPacket) sendPacketToViewers(new SetCursorItemPacket(cursorItem));
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, ItemStack item, ItemStack previous, boolean sendPacket) {
        for (Player player : getViewers()) {
            final EquipmentSlot equipmentSlot = getEquipmentSlot(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            EntityEquipEvent entityEquipEvent = new EntityEquipEvent(player, item, equipmentSlot);
            EventDispatcher.call(entityEquipEvent);
            item = entityEquipEvent.getEquippedItem();

            player.updateEquipmentAttributes(previous, item, equipmentSlot);
            player.syncEquipment(equipmentSlot, item);
        }

        super.UNSAFE_itemInsert(slot, item, previous, sendPacket);
    }

    @Override
    public void sendSlotRefresh(int slot, ItemStack item) {
        if (slot < 0 || slot > INVENTORY_SIZE)
            return; // Sanity check
        // See note in PlayerInventoryUtils about why we do this conversion
        boolean isPlayerInventorySlot = isPlayerInventorySlot(slot);
        int packetSlot = isPlayerInventorySlot
                ? convertMinestomSlotToPlayerInventorySlot(slot)
                : convertMinestomSlotToWindowSlot(slot);

        sendPacketToViewers(isPlayerInventorySlot
                ? new SetPlayerInventorySlotPacket(packetSlot, item)
                : new SetSlotPacket(0, 0, (short) packetSlot, item));
    }

    /**
     * Gets a {@link WindowItemsPacket} with all the items in the inventory.
     *
     * @return a {@link WindowItemsPacket} with inventory items
     */
    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < itemStacks.length; i++) {
            final int slot = convertMinestomSlotToWindowSlot(i);
            convertedSlots[slot] = itemStacks[i];
        }
        return new WindowItemsPacket(0, 0, List.of(convertedSlots), cursorItem);
    }

    @Override
    public boolean leftClick(Player player, int slot) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.leftClick(clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        callClickEvent(player, this, slot, ClickType.LEFT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean rightClick(Player player, int slot) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.rightClick(clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        callClickEvent(player, this, slot, ClickType.RIGHT_CLICK, clicked, cursor);
        return true;
    }

    @Override
    public boolean middleClick(Player player, int slot) {
        // TODO
        update();
        return false;
    }

    @Override
    public boolean drop(Player player, boolean all, int slot) {
        final ItemStack cursor = getCursorItem();
        final boolean outsideDrop = slot == -999;
        final ItemStack clicked = outsideDrop ? ItemStack.AIR : getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.drop(player, all, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        final ItemStack resultClicked = clickResult.getClicked();
        if (resultClicked != null && !outsideDrop) {
            setItemStack(slot, resultClicked);
        }
        setCursorItem(clickResult.getCursor());
        return true;
    }

    @Override
    public boolean shiftClick(Player player, int slot, int button) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(slot);
        final boolean hotBarClick = slot < 9;
        final int start = hotBarClick ? 9 : 0;
        final int end = hotBarClick ? getSize() - 9 : 9;
        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                this, this,
                start, end, 1,
                player, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean changeHeld(Player player, int slot, int key) {
        final ItemStack cursorItem = getCursorItem();
        if (!cursorItem.isAir()) return false;
        final ItemStack heldItem = getItemStack(key);
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.changeHeld(clicked, heldItem);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setItemStack(slot, clickResult.getClicked());
        setItemStack(key, clickResult.getCursor());
        callClickEvent(player, this, slot, ClickType.CHANGE_HELD, clicked, cursorItem);
        return true;
    }

    @Override
    public boolean dragging(Player player, List<Integer> slots, int button) {
        final ItemStack cursor = getCursorItem();

        final ItemStack clickResult = clickProcessor.dragging(player, this, slots, button, cursor);
        if (clickResult == null) {
            update();
            return false;
        }
        setCursorItem(clickResult);
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

    @Override
    public boolean doubleClick(Player player, int slot) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(slot);
        final InventoryClickResult clickResult = clickProcessor.doubleClick(this, this, player, slot, clicked, cursor);
        if (clickResult.isCancel()) {
            update();
            return false;
        }
        setCursorItem(clickResult.getCursor());
        update(); // FIXME: currently not properly client-predicted
        return true;
    }

}
