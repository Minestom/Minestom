package net.minestom.server.event.item;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PickupItemEvent extends Event implements CancellableEvent {

    private final LivingEntity livingEntity;
    private final ItemEntity itemEntity;

    private boolean cancelled;

    public PickupItemEvent(@NotNull LivingEntity livingEntity, @NotNull ItemEntity itemEntity) {
        this.livingEntity = livingEntity;
        this.itemEntity = itemEntity;
    }

    @NotNull
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    @NotNull
    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    /**
     * @deprecated use {@link #getItemEntity()} and {@link ItemEntity#getItemStack()}.
     */
    @Deprecated
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
}
