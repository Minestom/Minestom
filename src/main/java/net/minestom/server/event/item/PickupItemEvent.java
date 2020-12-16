package net.minestom.server.event.item;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PickupItemEvent extends Event implements CancellableEvent {

    private final LivingEntity livingEntity;
    private final ItemStack itemStack;

    private boolean cancelled;

    public PickupItemEvent(@NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack) {
        this.livingEntity = livingEntity;
        this.itemStack = itemStack;
    }

    @NotNull
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
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
