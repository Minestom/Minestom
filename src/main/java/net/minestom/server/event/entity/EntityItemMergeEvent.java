package net.minestom.server.event.entity;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called when two {@link ItemEntity} are merging their {@link ItemStack} together to form a sole entity.
 */
public class EntityItemMergeEvent extends CancellableEvent {

    private final ItemEntity source;
    private final ItemEntity merged;

    private ItemStack result;

    public EntityItemMergeEvent(ItemEntity source, ItemEntity merged, ItemStack result) {
        this.source = source;
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
    public ItemEntity getSource() {
        return source;
    }

    /**
     * Gets the entity who will be merged.
     * <p>
     * This entity will be removed after the event.
     *
     * @return the merged ItemEntity
     */
    public ItemEntity getMerged() {
        return merged;
    }

    /**
     * Gets the final item stack on the ground.
     *
     * @return the item stack
     */
    public ItemStack getResult() {
        return result;
    }

    /**
     * Changes the item stack which will appear on the ground.
     *
     * @param result the new item stack
     */
    public void setResult(ItemStack result) {
        this.result = result;
    }
}
