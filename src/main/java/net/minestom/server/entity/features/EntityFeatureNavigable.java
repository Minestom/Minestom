package net.minestom.server.entity.features;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.Navigator;
import org.jetbrains.annotations.NotNull;

public class EntityFeatureNavigable extends EntityFeatureBase {

    private final Navigator navigator;

    public EntityFeatureNavigable(Entity entity) {
        super(entity);
        this.navigator = new Navigator(entity);
    }

    @Override
    public void tick(long time) {
        navigator.tick(entity.getFeature(EntityFeatures.ATTRIBUTES).getAttributeValue(Attribute.MOVEMENT_SPEED));
    }

    @NotNull
    public Navigator getNavigator() {
        return navigator;
    }
}
