package net.minestom.server.event.entity;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.CancellableEvent;

/**
 * Called when two {@link ItemEntity} are merging their ItemStack together to form a sole entity
 */
public class EntityItemMergeEvent extends CancellableEvent {

    private ItemEntity source;
    private ItemEntity merged;

    public EntityItemMergeEvent(ItemEntity source, ItemEntity merged) {
        this.source = source;
        this.merged = merged;
    }

    /**
     * Get the entity who is receiving {@link #getMerged()} ItemStack
     * <p>
     * This can be used to get the final ItemEntity position
     *
     * @return the source ItemEntity
     */
    public ItemEntity getSource() {
        return source;
    }

    /**
     * Get the entity who will be merged
     * <p>
     * This entity will be removed after the event
     *
     * @return the merged ItemEntity
     */
    public ItemEntity getMerged() {
        return merged;
    }
}
