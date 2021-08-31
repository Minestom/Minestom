package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PickupItemEvent implements EntityEvent, ItemEvent, CancellableEvent {

    private final Entity entity;
    private final ItemEntity itemEntity;

    private boolean cancelled;

    public PickupItemEvent(@NotNull Entity entity, @NotNull ItemEntity itemEntity) {
        this.entity = entity;
        this.itemEntity = itemEntity;
    }

    @NotNull
    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    @NotNull
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
    public @NotNull Entity getEntity() {
        return entity;
    }
}
