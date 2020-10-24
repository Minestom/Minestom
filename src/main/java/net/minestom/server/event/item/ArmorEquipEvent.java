package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArmorEquipEvent extends Event {

    private final Entity entity;
    private ItemStack armorItem;
    private final ArmorSlot armorSlot;

    public ArmorEquipEvent(@NotNull Entity entity, @NotNull ItemStack armorItem, @NotNull ArmorSlot armorSlot) {
        this.entity = entity;
        this.armorItem = armorItem;
        this.armorSlot = armorSlot;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    @NotNull
    public ItemStack getArmorItem() {
        return armorItem;
    }

    public void setArmorItem(@NotNull ItemStack armorItem) {
        this.armorItem = armorItem;
    }

    @NotNull
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
