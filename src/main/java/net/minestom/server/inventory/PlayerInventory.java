package net.minestom.server.inventory;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public class PlayerInventory extends AbstractInventory implements EquipmentHandler {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_INVENTORY_SIZE = 36;

    protected final Player player;
    private ItemStack cursorItem = ItemStack.AIR;

    public PlayerInventory(@NotNull Player player) {
        super(INVENTORY_SIZE);
        this.player = player;
    }

    @Override
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        // fix packet slot to inventory slot conversion
        InventoryCondition condition = (p, slot, clickType, inventoryConditionResult) -> {
            final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
            inventoryCondition.accept(p, convertedSlot, clickType, inventoryConditionResult);
        };

        super.addInventoryCondition(condition);
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

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return getItemStack(player.getHeldSlot());
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        safeItemInsert(player.getHeldSlot(), itemStack);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return getItemStack(OFFHAND_SLOT);
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return getItemStack(HELMET_SLOT);
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return getItemStack(CHESTPLATE_SLOT);
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return getItemStack(LEGGINGS_SLOT);
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
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
        player.getPlayerConnection().sendPacket(createWindowItemsPacket());
    }

    /**
     * Gets the item in player cursor.
     *
     * @return the cursor item
     */
    @NotNull
    public ItemStack getCursorItem() {
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

    /**
     * Inserts an item safely (synchronized) in the appropriate slot.
     *
     * @param slot      an internal slot
     * @param itemStack the item to insert at the slot
     * @throws IllegalArgumentException if the slot {@code slot} does not exist
     * @throws NullPointerException     if {@code itemStack} is null
     */
    @Override
    protected synchronized void safeItemInsert(int slot, @NotNull ItemStack itemStack) {
        Check.argCondition(!MathUtils.isBetween(slot, 0, getSize()),
                "The slot {0} does not exist for player", slot);
        Check.notNull(itemStack, "The ItemStack cannot be null, you can set air instead");

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
            player.syncEquipment(equipmentSlot);
        }

        // Refresh slot
        update();
        // FIXME: replace update() to refreshSlot, currently not possible because our inventory click handling is not exactly the same as what the client expects
        //refreshSlot((short) slot);
    }

    /**
     * Sets an item from a packet slot.
     *
     * @param slot      a packet slot
     * @param offset    offset (generally 9 to ignore armor and craft slots)
     * @param itemStack the item stack to set
     */
    protected void setItemStack(int slot, int offset, @NotNull ItemStack itemStack) {
        final int convertedSlot = convertPlayerInventorySlot(slot, offset);
        setItemStack(convertedSlot, itemStack);
    }

    /**
     * Gets the item from a packet slot.
     *
     * @param slot   a packet slot
     * @param offset offset (generally 9 to ignore armor and craft slots)
     * @return the item in the specified slot
     */
    protected ItemStack getItemStack(int slot, int offset) {
        final int convertedSlot = convertPlayerInventorySlot(slot, offset);
        return this.itemStacks[convertedSlot];
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
        return new WindowItemsPacket((byte) 0, 0, convertedSlots, cursorItem);
    }

    @Override
    public boolean leftClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);

        final InventoryClickResult clickResult = clickProcessor.leftClick(player, this, convertedSlot, clicked, cursor);
        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(convertedSlot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, null, convertedSlot, ClickType.LEFT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean rightClick(@NotNull Player player, int slot) {
        final int convertedSlot = convertPlayerInventorySlot(slot, OFFSET);
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(convertedSlot);

        final InventoryClickResult clickResult = clickProcessor.rightClick(player, this, convertedSlot, clicked, cursor);
        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(convertedSlot, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, null, convertedSlot, ClickType.RIGHT_CLICK, clicked, cursor);

        return !clickResult.isCancel();
    }

    @Override
    public boolean middleClick(@NotNull Player player, int slot) {
        // TODO
        return false;
    }

    @Override
    public boolean drop(@NotNull Player player, boolean all, int slot, int button) {
        final ItemStack cursor = getCursorItem();
        final boolean outsideDrop = slot == -999;
        final ItemStack clicked = outsideDrop ? ItemStack.AIR : getItemStack(slot, OFFSET);

        final InventoryClickResult clickResult = clickProcessor.drop(player, this,
                all, slot, button, clicked, cursor);
        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        final ItemStack resultClicked = clickResult.getClicked();
        if (resultClicked != null && !outsideDrop)
            setItemStack(slot, OFFSET, resultClicked);
        setCursorItem(clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean shiftClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = getItemStack(slot, OFFSET);
        final boolean hotBarClick = convertSlot(slot, OFFSET) < 9;
        final int start = hotBarClick ? 9 : 0;
        final int end = hotBarClick ? getSize() - 9 : 8;
        final InventoryClickResult clickResult = clickProcessor.shiftClick(
                this, this,
                start, end, 1,
                player, slot, clicked, cursor);
        if (clickResult == null)
            return false;

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean changeHeld(@NotNull Player player, int slot, int key) {
        if (!getCursorItem().isAir())
            return false;

        final ItemStack heldItem = getItemStack(key);
        final ItemStack clicked = getItemStack(slot, OFFSET);

        final InventoryClickResult clickResult = clickProcessor.changeHeld(player, this, slot, key, clicked, heldItem);
        if (clickResult.doRefresh()) {
            sendSlotRefresh((short) slot, clicked);
        }

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setItemStack(key, clickResult.getCursor());

        if (!clickResult.isCancel())
            callClickEvent(player, null, slot, ClickType.CHANGE_HELD, clicked, getCursorItem());

        // Weird synchronization issue when omitted
        update();

        return !clickResult.isCancel();
    }

    @Override
    public boolean dragging(@NotNull Player player, int slot, int button) {
        final ItemStack cursor = getCursorItem();
        final ItemStack clicked = slot != -999 ? getItemStack(slot, OFFSET) : ItemStack.AIR;

        final InventoryClickResult clickResult = clickProcessor.dragging(player, this,
                slot, button,
                clicked, cursor, s -> getItemStack(s, OFFSET),
                (s, item) -> setItemStack(s, OFFSET, item));
        if (clickResult == null) {
            return false;
        }

        if (clickResult.doRefresh())
            update();

        setCursorItem(clickResult.getCursor());

        return !clickResult.isCancel();
    }

    @Override
    public boolean doubleClick(@NotNull Player player, int slot) {
        final ItemStack cursor = getCursorItem();
        final InventoryClickResult clickResult = clickProcessor.doubleClick(this, this, player, slot, cursor);
        if (clickResult == null)
            return false;
        if (clickResult.doRefresh())
            update();
        setCursorItem(clickResult.getCursor());
        return !clickResult.isCancel();
    }
}
