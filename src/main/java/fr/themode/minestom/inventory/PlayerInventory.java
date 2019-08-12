package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

public class PlayerInventory {

    private Player player;
    private ItemStack[] items = new ItemStack[45];

    public PlayerInventory(Player player) {
        this.player = player;
    }

    public ItemStack getItemStack(int slot) {
        ItemStack item = this.items[convertSlot(slot)];
        return item != null ? item : ItemStack.AIR_ITEM;
    }

    public void setItemStack(int slot, ItemStack itemStack) {
        this.items[convertSlot(slot)] = itemStack;
    }

    public void setHelmet(ItemStack itemStack) {
        this.items[5] = itemStack;
    }

    public void setChestplate(ItemStack itemStack) {
        this.items[6] = itemStack;
    }

    public void setLeggings(ItemStack itemStack) {
        this.items[7] = itemStack;
    }

    public void setBoots(ItemStack itemStack) {
        this.items[8] = itemStack;
    }

    public void update() {
        PlayerConnection playerConnection = player.getPlayerConnection();

        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = 0; // player inventory ID
        windowItemsPacket.count = (short) items.length;
        windowItemsPacket.items = items;
        playerConnection.sendPacket(windowItemsPacket);
    }

    private int convertSlot(int slot) {
        if (slot >= 0 && slot <= 9 * 4) {
            int row = slot / 9;
            int place = slot % 9;
            int converted = 9 * (4 - 1 - row) + place + 9;
            return converted;
        }
        return slot;
    }

}
