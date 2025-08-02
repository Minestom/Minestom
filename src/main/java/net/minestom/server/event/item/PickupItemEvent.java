package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;

public class PickupItemEvent implements EntityInstanceEvent, ItemEvent, CancellableEvent {

    private final LivingEntity livingEntity;
    private final ItemEntity itemEntity;

    private boolean cancelled;

    public PickupItemEvent(LivingEntity livingEntity, ItemEntity itemEntity) {
        this.livingEntity = livingEntity;
        this.itemEntity = itemEntity;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public ItemStack getItemStack() {
        return getItemEntity().getItemStack();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Entity getEntity() {
        return livingEntity;
    }
}
