package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Arrays;

public class PlayerInventory {

    private Player player;
    private ItemStack[] items = new ItemStack[45];

    public PlayerInventory(Player player) {
        this.player = player;

        Arrays.fill(items, ItemStack.AIR_ITEM);
    }

    public ItemStack getItemStack(int slot) {
        return this.items[slot];
    }

    public void setItemStack(int slot, ItemStack itemStack) {
        safeItemInsert(slot, itemStack);
    }

    public void setHelmet(ItemStack itemStack) {
        safeItemInsert(5, itemStack);
    }

    public void setChestplate(ItemStack itemStack) {
        safeItemInsert(6, itemStack);
    }

    public void setLeggings(ItemStack itemStack) {
        safeItemInsert(7, itemStack);
    }

    public void setBoots(ItemStack itemStack) {
        safeItemInsert(8, itemStack);
    }

    public void update() {
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createWindowItemsPacket());
    }

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            this.items[slot] = itemStack == null ? ItemStack.AIR_ITEM : itemStack;
        }
    }

    protected void setItemStack(int slot, int offset, ItemStack itemStack) {
        slot = convertSlot(slot, offset);
        // System.out.println("NEWSLOT: "+slot +" : "+itemStack.getItemId());
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
        System.out.println("DEBUG: " + slot);
        return slot;
    }

    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[45];
        Arrays.fill(convertedSlots, ItemStack.AIR_ITEM); // TODO armor and craft

        // Hotbar
        for (int i = 0; i < 9; i++) {
            convertedSlots[36 + i] = items[i];
        }

        // Inventory
        for (int i = 10; i < 9 + 9 * 3; i++) {
            convertedSlots[i] = items[i];
        }

        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = 0;
        windowItemsPacket.count = 45;
        windowItemsPacket.items = convertedSlots;
        return windowItemsPacket;
    }

}
