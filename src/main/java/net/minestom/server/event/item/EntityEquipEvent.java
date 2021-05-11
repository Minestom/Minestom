package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityEquipEvent extends Event {

    private final Entity entity;
    private ItemStack armorItem;
    private final EquipmentSlot slot;

    public EntityEquipEvent(@NotNull Entity entity, @NotNull ItemStack armorItem, @NotNull EquipmentSlot slot) {
        this.entity = entity;
        this.armorItem = armorItem;
        this.slot = slot;
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
    public EquipmentSlot getSlot() {
        return slot;
    }
}
