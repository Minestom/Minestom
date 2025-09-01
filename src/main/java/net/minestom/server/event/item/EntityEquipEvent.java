package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;

public class EntityEquipEvent implements EntityInstanceEvent, ItemEvent {

    private final Entity entity;
    private ItemStack equippedItem;
    private final EquipmentSlot slot;

    public EntityEquipEvent(Entity entity, ItemStack equippedItem, EquipmentSlot slot) {
        this.entity = entity;
        this.equippedItem = equippedItem;
        this.slot = slot;
    }

    public ItemStack getEquippedItem() {
        return equippedItem;
    }

    public void setEquippedItem(ItemStack armorItem) {
        this.equippedItem = armorItem;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    /**
     * Same as {@link #getEquippedItem()}.
     */
    @Override
    public ItemStack getItemStack() {
        return equippedItem;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }
}
