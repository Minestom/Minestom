package net.minestom.server.event.entity;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.CancellableEvent;

public class EntityItemMergeEvent extends CancellableEvent {

    private ItemEntity source;
    private ItemEntity merged;

    public EntityItemMergeEvent(ItemEntity source, ItemEntity merged) {
        this.source = source;
        this.merged = merged;
    }

    public ItemEntity getSource() {
        return source;
    }

    public ItemEntity getMerged() {
        return merged;
    }
}
