package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.ArmorEquipEvent;
import fr.themode.minestom.inventory.click.InventoryClickLoopHandler;
import fr.themode.minestom.inventory.click.InventoryClickProcessor;
import fr.themode.minestom.inventory.click.InventoryClickResult;
import fr.themode.minestom.inventory.condition.InventoryCondition;
import fr.themode.minestom.inventory.condition.InventoryConditionResult;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;
import fr.themode.minestom.net.packet.server.play.EntityEquipmentPacket;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Arrays;

import static fr.themode.minestom.utils.inventory.PlayerInventoryUtils.*;

public class PlayerInventory implements InventoryModifier, InventoryClickHandler {

    public static final int INVENTORY_SIZE = 46;

    private Player player;
    private ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private ItemStack cursorItem = ItemStack.AIR_ITEM;

    private InventoryCondition inventoryCondition;
    private InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    public PlayerInventory(Player player) {
        this.player = player;

        Arrays.fill(items, ItemStack.AIR_ITEM);
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
    public InventoryCondition getInventoryCondition() {
        return inventoryCondition;
    }

    @Override
    public void setInventoryCondition(InventoryCondition inventoryCondition) {
        this.inventoryCondition = inventoryCondition;
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
        player.syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    public ItemStack getItemInOffHand() {
        return getItemStack(OFFHAND_SLOT);
    }

    public void setItemInOffHand(ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    public ItemStack getHelmet() {
        return getItemStack(HELMET_SLOT);
    }

    public void setHelmet(ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    public ItemStack getChestplate() {
        return getItemStack(CHESTPLATE_SLOT);
    }

    public void setChestplate(ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    public ItemStack getLeggings() {
        return getItemStack(LEGGINGS_SLOT);
    }

    public void setLeggings(ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    public ItemStack getBoots() {
        return getItemStack(BOOTS_SLOT);
    }

    public void setBoots(ItemStack itemStack) {
        safeItemInsert(BOOTS_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
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
        }
        return ItemStack.AIR_ITEM;
    }

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.AIR_ITEM : itemStack;

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
            //System.out.println("INSERT: " + slot);
            //sendSlotRefresh((short) slot, itemStack);

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
        } else if (slot >= CRAFT_SLOT_1 && slot <= CRAFT_RESULT) { // Crafting 36-40
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

        InventoryClickResult clickResult = clickProcessor.leftClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void rightClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.rightClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void middleClick(Player player, int slot) {

    }

    @Override
    public void dropOne(Player player, int slot) {

    }

    @Override
    public void dropItemStack(Player player, int slot) {

    }

    @Override
    public void shiftClick(Player player, int slot) {
        ItemStack clicked = getItemStack(slot, OFFSET);
        ItemStack cursorItem = getCursorItem(); // Not used

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                setItemStack(slot, OFFSET, clicked);
                setCursorItem(cursorItem);
                // Refresh client slot
                sendSlotRefresh((short) slot, clicked);
                return;
            }
        }
        // End condition

        if (clicked.isAir())
            return;

        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultClicked = clicked.clone();
        boolean filled = false;
        for (int i = 0; i < items.length; i++) {
            int index = i < 9 ? i + 9 : i - 9;
            ItemStack item = items[index];
            StackingRule itemRule = item.getStackingRule();
            if (itemRule.canBeStacked(item, clicked)) {
                int amount = item.getAmount();
                if (!clickedRule.canApply(clicked, amount + 1))
                    continue;
                int totalAmount = resultClicked.getAmount() + amount;
                if (!clickedRule.canApply(clicked, totalAmount)) {
                    item = itemRule.apply(item, itemRule.getMaxSize());
                    setItemStack(index, OFFSET, item);

                    resultClicked = clickedRule.apply(resultClicked, totalAmount - clickedRule.getMaxSize());
                    filled = false;
                    continue;
                } else {
                    resultClicked = clickedRule.apply(resultClicked, totalAmount);
                    setItemStack(index, resultClicked);

                    item = itemRule.apply(item, 0);
                    setItemStack(slot, OFFSET, item);
                    filled = true;
                    break;
                }
            } else if (item.isAir()) {
                // Switch
                setItemStack(index, resultClicked);
                setItemStack(slot, OFFSET, ItemStack.AIR_ITEM);
                filled = true;
                break;
            }
        }
        if (!filled) {
            setItemStack(slot, OFFSET, resultClicked);
        }
    }

    @Override
    public void changeHeld(Player player, int slot, int key) {
        if (!getCursorItem().isAir())
            return;

        ItemStack heldItem = getItemStack(key);
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.changeHeld(getInventoryCondition(), player, slot, clicked, heldItem);

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

        InventoryClickResult clickResult = clickProcessor.dragging(getInventoryCondition(), player,
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

        InventoryClickResult clickResult = clickProcessor.doubleClick(getInventoryCondition(), player, slot, cursor,
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
