package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when two {@link ItemEntity} are merging their {@link ItemStack} together to form a sole entity.
 */
public record EntityItemMergeEvent(@NotNull ItemEntity entity, @NotNull ItemEntity merged, @NotNull ItemStack result, boolean cancelled) implements EntityInstanceEvent, CancellableEvent<EntityItemMergeEvent> {

    public EntityItemMergeEvent(@NotNull ItemEntity source, @NotNull ItemEntity merged, @NotNull ItemStack result) {
        this(source, merged, result, false);
    }

    /**
     * Gets the {@link ItemEntity} who is receiving {@link #merged()}.
     * <p>
     * This can be used to get the final ItemEntity position.
     *
     * @return the source ItemEntity
     */
    @Override
    public @NotNull ItemEntity entity() {
        return entity;
    }

    /**
     * Gets the entity who will be merged.
     * <p>
     * This entity will be removed after the event.
     *
     * @return the merged ItemEntity
     */
    @Override
    public @NotNull ItemEntity merged() {
        return merged;
    }

    /**
     * Gets the final item stack on the ground.
     *
     * @return the item stack
     */
    public @NotNull ItemStack result() {
        return result;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<EntityItemMergeEvent> {
        private final ItemEntity entity;
        private final ItemEntity merged;
        private ItemStack result;

        private boolean cancelled;

        public Mutator(EntityItemMergeEvent event) {
            this.entity = event.entity;
            this.merged = event.merged;
            this.result = event.result;
            this.cancelled = event.cancelled;
        }

        /**
         * Gets the final item stack on the ground.
         *
         * @return the item stack
         */
        public @NotNull ItemStack getResult() {
            return result;
        }

        /**
         * Changes the item stack which will appear on the ground.
         *
         * @param result the new item stack
         */
        public void setResult(@NotNull ItemStack result) {
            this.result = result;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public @NotNull EntityItemMergeEvent mutated() {
            return new EntityItemMergeEvent(this.entity, this.merged, this.result, this.cancelled);
        }
    }
}
