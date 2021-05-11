package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityEquipEvent extends Event {

    private final Entity entity;
    private ItemStack equippedItem;
    private final EquipmentSlot slot;

    public EntityEquipEvent(@NotNull Entity entity, @NotNull ItemStack equippedItem, @NotNull EquipmentSlot slot) {
        this.entity = entity;
        this.equippedItem = equippedItem;
        this.slot = slot;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }

    @NotNull
    public ItemStack getEquippedItem() {
        return equippedItem;
    }

    public void setEquippedItem(@NotNull ItemStack armorItem) {
        this.equippedItem = armorItem;
    }

    @NotNull
    public EquipmentSlot getSlot() {
        return slot;
    }
}
