package net.minestom.server.event.item;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.item.ItemStackUtils;

public class ArmorEquipEvent extends Event {

    private LivingEntity livingEntity;
    private ItemStack armorItem;
    private ArmorSlot armorSlot;

    public ArmorEquipEvent(LivingEntity livingEntity, ItemStack armorItem, ArmorSlot armorSlot) {
        this.livingEntity = livingEntity;
        this.armorItem = armorItem;
        this.armorSlot = armorSlot;
    }

    public LivingEntity getEntity() {
        return livingEntity;
    }

    public ItemStack getArmorItem() {
        return armorItem;
    }

    public void setArmorItem(ItemStack armorItem) {
        this.armorItem = ItemStackUtils.notNull(armorItem);
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
