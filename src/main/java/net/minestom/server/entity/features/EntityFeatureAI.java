package net.minestom.server.entity.features;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.EntityAIGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EntityFeatureAI extends EntityFeatureBase implements EntityAI {

    private final Set<EntityAIGroup> aiGroups = new CopyOnWriteArraySet<>();
    private Entity target;

    public EntityFeatureAI(Entity entity) {
        super(entity);
    }

    @Override
    public void tick(long time) {
        aiTick(time);
    }

    @Override
    public Collection<EntityAIGroup> getAIGroups() {
        return aiGroups;
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }
}
