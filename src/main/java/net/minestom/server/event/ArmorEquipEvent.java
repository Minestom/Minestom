package net.minestom.server.event;

import net.minestom.server.item.ItemStack;

public class ArmorEquipEvent extends Event {

    private ItemStack armorItem;
    private ArmorSlot armorSlot;

    public ArmorEquipEvent(ItemStack armorItem, ArmorSlot armorSlot) {
        this.armorItem = armorItem;
        this.armorSlot = armorSlot;
    }

    public ItemStack getArmorItem() {
        return armorItem;
    }

    public void setArmorItem(ItemStack armorItem) {
        this.armorItem = armorItem;
    }

    public ArmorSlot getArmorSlot() {
        return armorSlot;
    }

    public enum ArmorSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }
}
