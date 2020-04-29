package net.minestom.server.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.ArmorEquipEvent;
import net.minestom.server.inventory.click.InventoryClickLoopHandler;
import net.minestom.server.inventory.click.InventoryClickProcessor;
import net.minestom.server.inventory.click.InventoryClickResult;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public class PlayerInventory implements InventoryModifier, InventoryClickHandler {

    public static final int INVENTORY_SIZE = 46;

    private Player player;
    private ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private ItemStack cursorItem = ItemStack.getAirItem();

    private List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    private InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    public PlayerInventory(Player player) {
        this.player = player;

        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.getAirItem();
        }
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return this.items[slot];
    }

    @Override
    public ItemStack[] getItemStacks() {
        return Arrays.copyOf(items, items.length);
    }

    @Override
    public List<InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    @Override
    public void addInventoryCondition(InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    @Override
    public void setItemStack(int slot, ItemStack itemStack) {
        safeItemInsert(slot, itemStack);
    }

    @Override
    public boolean addItemStack(ItemStack itemStack) {
        synchronized (this) {
            StackingRule stackingRule = itemStack.getStackingRule();
            for (int i = 0; i < items.length - 10; i++) {
                ItemStack item = items[i];
                StackingRule itemStackingRule = item.getStackingRule();
                if (itemStackingRule.canBeStacked(itemStack, item)) {
                    int itemAmount = itemStackingRule.getAmount(item);
                    if (itemAmount == stackingRule.getMaxSize())
                        continue;
                    int itemStackAmount = itemStackingRule.getAmount(itemStack);
                    int totalAmount = itemStackAmount + itemAmount;
                    if (!stackingRule.canApply(itemStack, totalAmount)) {
                        item = itemStackingRule.apply(item, itemStackingRule.getMaxSize());

                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        itemStack = stackingRule.apply(itemStack, totalAmount - stackingRule.getMaxSize());
                    } else {
                        item.setAmount((byte) totalAmount);
                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        return true;
                    }
                } else if (item.isAir()) {
                    setItemStack(i, itemStack);
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack getItemInMainHand() {
        return getItemStack(player.getHeldSlot());
    }

    public void setItemInMainHand(ItemStack itemStack) {
        safeItemInsert(player.getHeldSlot(), itemStack);
    }

    public ItemStack getItemInOffHand() {
        return getItemStack(OFFHAND_SLOT);
    }

    public void setItemInOffHand(ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
    }

    public ItemStack getHelmet() {
        return getItemStack(HELMET_SLOT);
    }

    public void setHelmet(ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
    }

    public ItemStack getChestplate() {
        return getItemStack(CHESTPLATE_SLOT);
    }

    public void setChestplate(ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
    }

    public ItemStack getLeggings() {
        return getItemStack(LEGGINGS_SLOT);
    }

    public void setLeggings(ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
    }

    public ItemStack getBoots() {
        return getItemStack(BOOTS_SLOT);
    }

    public void setBoots(ItemStack itemStack) {
        safeItemInsert(BOOTS_SLOT, itemStack);
    }

    public void update() {
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createWindowItemsPacket());
    }

    public void refreshSlot(int slot) {
        sendSlotRefresh((short) convertToPacketSlot(slot), getItemStack(slot));
    }

    public ItemStack getCursorItem() {
        return cursorItem;
    }

    public void setCursorItem(ItemStack cursorItem) {
        this.cursorItem = cursorItem;
    }

    public ItemStack getEquipment(EntityEquipmentPacket.Slot slot) {
        switch (slot) {
            case MAIN_HAND:
                return getItemInMainHand();
            case OFF_HAND:
                return getItemInOffHand();
            case HELMET:
                return getHelmet();
            case CHESTPLATE:
                return getChestplate();
            case LEGGINGS:
                return getLeggings();
            case BOOTS:
                return getBoots();
            default:
                throw new NullPointerException("Equipment slot cannot be null");
        }
    }

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.getAirItem() : itemStack;

            EntityEquipmentPacket.Slot equipmentSlot;

            if (slot == player.getHeldSlot()) {
                equipmentSlot = EntityEquipmentPacket.Slot.MAIN_HAND;
            } else if (slot == OFFHAND_SLOT) {
                equipmentSlot = EntityEquipmentPacket.Slot.OFF_HAND;
            } else if (slot == HELMET_SLOT) {
                equipmentSlot = EntityEquipmentPacket.Slot.HELMET;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
                player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
                itemStack = armorEquipEvent.getArmorItem();
            } else if (slot == CHESTPLATE_SLOT) {
                equipmentSlot = EntityEquipmentPacket.Slot.CHESTPLATE;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
                player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
                itemStack = armorEquipEvent.getArmorItem();
            } else if (slot == LEGGINGS_SLOT) {
                equipmentSlot = EntityEquipmentPacket.Slot.LEGGINGS;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
                player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
                itemStack = armorEquipEvent.getArmorItem();
            } else if (slot == BOOTS_SLOT) {
                equipmentSlot = EntityEquipmentPacket.Slot.BOOTS;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
                player.callEvent(ArmorEquipEvent.class, armorEquipEvent);
                itemStack = armorEquipEvent.getArmorItem();
            } else {
                equipmentSlot = null;
            }

            if (itemStack != null) {
                this.items[slot] = itemStack;
            }

            // Refresh inventory items
            update();

            // Sync equipment
            if (equipmentSlot != null) {
                player.syncEquipment(equipmentSlot);
            }
        }
    }

    protected void setItemStack(int slot, int offset, ItemStack itemStack) {
        slot = convertSlot(slot, offset);
        safeItemInsert(slot, itemStack);
    }

    protected ItemStack getItemStack(int slot, int offset) {
        slot = convertSlot(slot, offset);
        return this.items[slot];
    }

    protected int convertToPacketSlot(int slot) {
        if (slot > -1 && slot < 9) { // Held bar 0-9
            slot = slot + 36;
        } else if (slot > 8 && slot < 36) { // Inventory 9-35
            slot = slot;
        } else if (slot >= CRAFT_RESULT && slot <= CRAFT_SLOT_4) { // Crafting 36-40
            slot = slot - 36;
        } else if (slot >= HELMET_SLOT && slot <= BOOTS_SLOT) { // Armor 41-44
            slot = slot - 36;
        } else if (slot == OFFHAND_SLOT) { // Off hand
            slot = 45;
        }
        return slot;
    }

    private void sendSlotRefresh(short slot, ItemStack itemStack) {
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = (byte) (slot > 35 && slot < INVENTORY_SIZE ? 0 : -2);
        setSlotPacket.slot = slot;
        setSlotPacket.itemStack = itemStack;
        player.getPlayerConnection().sendPacket(setSlotPacket);
    }

    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[INVENTORY_SIZE];

        for (int i = 0; i < items.length; i++) {
            int slot = convertToPacketSlot(i);
            convertedSlots[slot] = items[i];
        }

        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = 0;
        windowItemsPacket.count = INVENTORY_SIZE;
        windowItemsPacket.items = convertedSlots;
        return windowItemsPacket;
    }

    @Override
    public void leftClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(convertSlot(slot, OFFSET));

        InventoryClickResult clickResult = clickProcessor.leftClick(getInventoryConditions(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void rightClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.rightClick(getInventoryConditions(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void middleClick(Player player, int slot) {

    }

    @Override
    public void drop(Player player, int mode, int slot, int button) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = slot == -999 ? null : getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.drop(getInventoryConditions(), player,
                mode, slot, button, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        ItemStack resultClicked = clickResult.getClicked();
        if (resultClicked != null)
            setItemStack(slot, OFFSET, resultClicked);
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void shiftClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        boolean hotbarClick = convertToPacketSlot(slot) < 9;
        InventoryClickResult clickResult = clickProcessor.shiftClick(getInventoryConditions(), player, slot, clicked, cursor,
                new InventoryClickLoopHandler(0, items.length, 1,
                        i -> {
                            if (hotbarClick) {
                                return i < 9 ? i + 9 : i - 9;
                            } else {
                                return convertSlot(i, OFFSET);
                            }
                        },
                        index -> getItemStack(index, OFFSET),
                        (index, itemStack) -> setItemStack(index, OFFSET, itemStack)));

        if (clickResult == null)
            return;

        if (clickResult.doRefresh())
            update();

        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void changeHeld(Player player, int slot, int key) {
        if (!getCursorItem().isAir())
            return;

        ItemStack heldItem = getItemStack(key);
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.changeHeld(getInventoryConditions(), player, slot, clicked, heldItem);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setItemStack(key, clickResult.getCursor());
    }

    @Override
    public void dragging(Player player, int slot, int button) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = null;
        if (slot != -999)
            clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.dragging(getInventoryConditions(), player,
                slot, button,
                clicked, cursor, s -> getItemStack(s, OFFSET),
                (s, item) -> setItemStack(s, OFFSET, item));

        if (clickResult == null)
            return;

        if (clickResult.doRefresh())
            update();

        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void doubleClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();

        InventoryClickResult clickResult = clickProcessor.doubleClick(getInventoryConditions(), player, slot, cursor,
                new InventoryClickLoopHandler(0, items.length, 1,
                        i -> i < 9 ? i + 9 : i - 9,
                        index -> items[index],
                        (index, itemStack) -> setItemStack(index, itemStack)));

        if (clickResult == null)
            return;

        if (clickResult.doRefresh())
            update();

        setCursorItem(clickResult.getCursor());
    }
}
