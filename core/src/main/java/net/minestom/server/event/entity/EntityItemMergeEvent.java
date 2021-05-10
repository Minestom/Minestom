package net.minestom.server.event.entity;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when two {@link ItemEntity} are merging their {@link ItemStack} together to form a sole entity.
 */
public class EntityItemMergeEvent extends EntityEvent implements CancellableEvent {

    private final ItemEntity merged;
    private ItemStack result;

    private boolean cancelled;

    public EntityItemMergeEvent(@NotNull ItemEntity source, @NotNull ItemEntity merged, @NotNull ItemStack result) {
        super(source);
        this.merged = merged;
        this.result = result;
    }

    /**
     * Gets the {@link ItemEntity} who is receiving {@link #getMerged()}.
     * <p>
     * This can be used to get the final ItemEntity position.
     *
     * @return the source ItemEntity
     */
    @NotNull
    @Override
    public ItemEntity getEntity() {
        return (ItemEntity) entity;
    }

    /**
     * Gets the entity who will be merged.
     * <p>
     * This entity will be removed after the event.
     *
     * @return the merged ItemEntity
     */
    @NotNull
    public ItemEntity getMerged() {
        return merged;
    }

    /**
     * Gets the final item stack on the ground.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack getResult() {
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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
