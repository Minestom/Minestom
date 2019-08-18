package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Arrays;

public class PlayerInventory implements InventoryModifier, InventoryClickHandler {

    public static final int INVENTORY_SIZE = 46;

    private static final byte ITEM_MAX_SIZE = 127;

    private static final int OFFSET = 9;

    private static final int CRAFT_SLOT_1 = 36;
    private static final int CRAFT_SLOT_2 = 37;
    private static final int CRAFT_SLOT_3 = 38;
    private static final int CRAFT_SLOT_4 = 39;
    private static final int CRAFT_RESULT = 40;
    private static final int HELMET_SLOT = 41;
    private static final int CHESTPLATE_SLOT = 42;
    private static final int LEGGINGS_SLOT = 43;
    private static final int BOOTS_SLOT = 44;
    private static final int OFFHAND_SLOT = 45;

    private Player player;
    private ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private ItemStack cursorItem = ItemStack.AIR_ITEM;

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
    public void setItemStack(int slot, ItemStack itemStack) {
        safeItemInsert(slot, itemStack);
    }

    @Override
    public boolean addItemStack(ItemStack itemStack) {
        synchronized (this) {
            for (int i = 0; i < items.length - 10; i++) {
                ItemStack item = items[i];
                if (item.isAir()) {
                    setItemStack(i, itemStack);
                    return true;
                } else if (itemStack.isSimilar(item)) {
                    int itemAmount = item.getAmount();
                    if (itemAmount == ITEM_MAX_SIZE)
                        continue;
                    int totalAmount = itemStack.getAmount() + itemAmount;
                    if (totalAmount > ITEM_MAX_SIZE) {
                        item.setAmount((byte) ITEM_MAX_SIZE);
                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        itemStack.setAmount((byte) (totalAmount - ITEM_MAX_SIZE));
                    } else {
                        item.setAmount((byte) totalAmount);
                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setHelmet(ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
    }

    public void setChestplate(ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
    }

    public void setLeggings(ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
    }

    public void setBoots(ItemStack itemStack) {
        safeItemInsert(BOOTS_SLOT, itemStack);
    }

    public void setItemInMainHand(ItemStack itemStack) {
        safeItemInsert(player.getHeldSlot(), itemStack);
    }

    public void setItemInOffHand(ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
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

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.AIR_ITEM : itemStack;
            this.items[slot] = itemStack;
            // System.out.println("INSERT: " + slot);
            //sendSlotRefresh((short) slot, itemStack);
            update();
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


    protected int convertSlot(int slot, int offset) {
        switch (slot) {
            case 1:
                return CRAFT_SLOT_1 + 1;
            case 2:
                return CRAFT_SLOT_2 + 1;
            case 3:
                return CRAFT_SLOT_3 + 1;
            case 4:
                return CRAFT_SLOT_4 + 1;
        }
        //System.out.println("ENTRY: " + slot + " | " + offset);
        final int rowSize = 9;
        slot -= offset;
        if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot = slot % 9;
        } else {
            slot = slot + rowSize;
        }
        //System.out.println("CONVERT: " + slot);
        return slot;
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
        ItemStack cursorItem = getCursorItem();
        ItemStack clicked = getItemStack(convertSlot(slot, OFFSET));

        if (!cursorItem.isAir()) {
            if (slot == 0 || slot == 6 || slot == 7 || slot == 8) {
                return; // Disable putting item on CRAFTING_RESULT and chestplate/leggings/boots slots
            }
        }

        if (cursorItem.isAir() && clicked.isAir())
            return;

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorItem.isSimilar(clicked)) {
            resultCursor = cursorItem.clone();
            resultClicked = clicked.clone();
            int totalAmount = cursorItem.getAmount() + clicked.getAmount();
            if (totalAmount > ITEM_MAX_SIZE) {
                resultCursor.setAmount((byte) (totalAmount - ITEM_MAX_SIZE));
                resultClicked.setAmount((byte) ITEM_MAX_SIZE);
            } else {
                resultCursor = ItemStack.AIR_ITEM;
                resultClicked.setAmount((byte) totalAmount);
            }
        } else {
            resultCursor = clicked.clone();
            resultClicked = cursorItem.clone();
        }

        setItemStack(slot, OFFSET, resultClicked);
        setCursorItem(resultCursor);
    }

    @Override
    public void rightClick(Player player, int slot) {
        ItemStack cursorItem = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        if (!cursorItem.isAir()) {
            if (slot == 0 || slot == 6 || slot == 7 || slot == 8) {
                return; // Disable putting item on CRAFTING_RESULT and chestplate/leggings/boots slots
            }
        }

        if (cursorItem.isAir() && clicked.isAir())
            return;

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorItem.isSimilar(clicked)) {
            resultClicked = clicked.clone();
            int amount = clicked.getAmount() + 1;
            if (amount > ITEM_MAX_SIZE) {
                return;
            } else {
                resultCursor = cursorItem.clone();
                resultCursor.setAmount((byte) (resultCursor.getAmount() - 1));
                if (resultCursor.getAmount() < 1)
                    resultCursor = ItemStack.AIR_ITEM;
                resultClicked.setAmount((byte) amount);
            }
        } else {
            if (cursorItem.isAir()) {
                int amount = (int) Math.ceil((double) clicked.getAmount() / 2d);
                resultCursor = clicked.clone();
                resultCursor.setAmount((byte) amount);
                resultClicked = clicked.clone();
                resultClicked.setAmount((byte) (clicked.getAmount() / 2));
            } else {
                if (clicked.isAir()) {
                    int amount = cursorItem.getAmount();
                    resultCursor = cursorItem.clone();
                    resultCursor.setAmount((byte) (amount - 1));
                    if (resultCursor.getAmount() < 1)
                        resultCursor = ItemStack.AIR_ITEM;
                    resultClicked = cursorItem.clone();
                    resultClicked.setAmount((byte) 1);
                } else {
                    resultCursor = clicked.clone();
                    resultClicked = cursorItem.clone();
                }
            }
        }

        setItemStack(slot, OFFSET, resultClicked);
        setCursorItem(resultCursor);
    }

    @Override
    public void shiftClick(Player player, int slot) {
        ItemStack clicked = getItemStack(slot, OFFSET);

        if (clicked.isAir())
            return;

        ItemStack resultClicked = clicked.clone();
        boolean filled = false;
        for (int i = 0; i < items.length; i++) {
            int index = i < 9 ? i + 9 : i - 9;
            ItemStack item = items[index];
            if (item.isSimilar(clicked)) {
                int amount = item.getAmount();
                if (amount == ITEM_MAX_SIZE)
                    continue;
                int totalAmount = resultClicked.getAmount() + amount;
                if (totalAmount > ITEM_MAX_SIZE) {
                    item.setAmount((byte) ITEM_MAX_SIZE);
                    setItemStack(index, item);
                    resultClicked.setAmount((byte) (totalAmount - ITEM_MAX_SIZE));
                    filled = false;
                    continue;
                } else {
                    resultClicked.setAmount((byte) totalAmount);
                    setItemStack(index, resultClicked);
                    setItemStack(slot, OFFSET, ItemStack.AIR_ITEM);
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

        ItemStack resultClicked;
        ItemStack resultHeld;

        if (clicked.isAir()) {
            // Set held item [key] to slot
            resultClicked = ItemStack.AIR_ITEM;
            resultHeld = clicked.clone();
        } else {
            if (heldItem.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.AIR_ITEM;
                resultHeld = clicked.clone();
            } else {
                // Otherwise replace held item and held
                resultClicked = heldItem.clone();
                resultHeld = clicked.clone();
            }
        }

        setItemStack(slot, OFFSET, resultClicked);
        setItemStack(key, resultHeld);
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
    public void doubleClick(Player player, int slot) {
        ItemStack cursorItem = getCursorItem().clone();
        if (cursorItem.isAir())
            return;

        int amount = cursorItem.getAmount();

        if (amount == ITEM_MAX_SIZE)
            return;

        for (int i = 0; i < items.length; i++) {
            int index = i < 9 ? i + 9 : i - 9;
            if (index == slot)
                continue;
            if (amount == ITEM_MAX_SIZE)
                break;
            ItemStack item = items[index];
            if (cursorItem.isSimilar(item)) {
                int totalAmount = amount + item.getAmount();
                if (totalAmount > ITEM_MAX_SIZE) {
                    cursorItem.setAmount((byte) ITEM_MAX_SIZE);
                    item.setAmount((byte) (totalAmount - ITEM_MAX_SIZE));
                    setItemStack(index, item);
                } else {
                    cursorItem.setAmount((byte) totalAmount);
                    setItemStack(index, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        setCursorItem(cursorItem);
    }
}
