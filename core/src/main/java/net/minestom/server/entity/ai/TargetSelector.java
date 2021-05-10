package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The target selector is called each time the entity receives an "attack" instruction
 * without having a target.
 */
public abstract class TargetSelector {

    protected final EntityCreature entityCreature;

    public TargetSelector(@NotNull EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    /**
     * Finds the target.
     * <p>
     * Returning null means that this target selector didn't find any entity,
     * the next {@link TargetSelector} will be called until the end of the list or an entity is found.
     *
     * @return the target, null if not any
     */
    @Nullable
    public abstract Entity findTarget();

    /**
     * Gets the entity linked to this target selector.
     *
     * @return the entity
     */
    @NotNull
    public EntityCreature getEntityCreature() {
        return entityCreature;
    }
}
