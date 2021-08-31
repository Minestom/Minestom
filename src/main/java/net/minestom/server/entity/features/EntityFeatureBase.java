package net.minestom.server.entity.features;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public abstract class EntityFeatureBase implements Tickable {

    protected final Entity entity;

    public EntityFeatureBase(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void tick(long time) {

    }

    public void onSpawn() {

    }

    public void onRemoval() {

    }

    public void onAddViewer(Player player) {

    }

    public void onRemoveViewer(Player player) {

    }

}
