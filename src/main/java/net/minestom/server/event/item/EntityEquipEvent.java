package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityEquipEvent implements EntityEvent, EntityInstanceEvent, ItemEvent {

    private final Entity entity;
    private ItemStack equippedItem;
    private final EquipmentSlot slot;

    public EntityEquipEvent(@NotNull Entity entity, @NotNull ItemStack equippedItem, @NotNull EquipmentSlot slot) {
        this.entity = entity;
        this.equippedItem = equippedItem;
        this.slot = slot;
    }

    public @NotNull ItemStack getEquippedItem() {
        return equippedItem;
    }

    public void setEquippedItem(@NotNull ItemStack armorItem) {
        this.equippedItem = armorItem;
    }

    public @NotNull EquipmentSlot getSlot() {
        return slot;
    }

    /**
     * Same as {@link #getEquippedItem()}.
     */
    @Override
    public @NotNull ItemStack getItemStack() {
        return equippedItem;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
}
