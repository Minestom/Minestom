package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Arrays;

public class PlayerInventory implements InventoryModifier {

    private static final int TOTAL_INVENTORY_SIZE = 46;

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
    private ItemStack[] items = new ItemStack[TOTAL_INVENTORY_SIZE];

    public PlayerInventory(Player player) {
        this.player = player;

        Arrays.fill(items, ItemStack.AIR_ITEM);
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return this.items[slot];
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
                    // TODO check max stack size
                    int itemAmount = item.getAmount();
                    if (itemAmount == 64)
                        continue;
                    int totalAmount = itemStack.getAmount() + itemAmount;
                    if (totalAmount > 64) {
                        item.setAmount((byte) 64);
                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        itemStack.setAmount((byte) (totalAmount - 64));
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

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.AIR_ITEM : itemStack;
            this.items[slot] = itemStack;
            System.out.println("INSERT: " + slot);
            sendSlotRefresh((short) slot, itemStack);
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
        final int rowSize = 9;
        slot -= offset;
        if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot = slot % 9;
        } else {
            slot = slot + rowSize;
        }
        return slot;
    }

    private int convertToPacketSlot(int slot) {
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
        setSlotPacket.windowId = (byte) (slot > 35 && slot < TOTAL_INVENTORY_SIZE ? 0 : -2);
        setSlotPacket.slot = slot;
        setSlotPacket.itemStack = itemStack;
        player.getPlayerConnection().sendPacket(setSlotPacket);
    }

    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[TOTAL_INVENTORY_SIZE];
        Arrays.fill(convertedSlots, ItemStack.AIR_ITEM); // TODO armor and craft

        for (int i = 0; i < items.length; i++) {
            int slot = convertToPacketSlot(i);
            convertedSlots[slot] = items[i];
        }

        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = 0;
        windowItemsPacket.count = TOTAL_INVENTORY_SIZE;
        windowItemsPacket.items = convertedSlots;
        return windowItemsPacket;
    }

}
