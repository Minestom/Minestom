package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record PickupItemEvent(@NotNull LivingEntity livingEntity, @NotNull ItemEntity itemEntity, boolean cancelled) implements EntityInstanceEvent, ItemEvent, CancellableEvent<PickupItemEvent> {

    public PickupItemEvent(@NotNull LivingEntity livingEntity, @NotNull ItemEntity itemEntity) {
        this(livingEntity, itemEntity, false);
    }

    @NotNull
    public ItemStack itemStack() {
        return itemEntity.getItemStack();
    }

    @Override
    public @NotNull Entity entity() {
        return livingEntity;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<PickupItemEvent> {
        public Mutator(@NotNull PickupItemEvent event) {
            super(event);
        }

        @Override
        public @NotNull PickupItemEvent mutated() {
            return new PickupItemEvent(this.event.livingEntity, this.event.itemEntity, this.isCancelled());
        }
    }
}
