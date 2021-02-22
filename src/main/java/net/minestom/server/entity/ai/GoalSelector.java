package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GoalSelector {

    protected EntityCreature entityCreature;

    public GoalSelector(@NotNull EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    /**
     * Whether or not this {@link GoalSelector} should start.
     *
     * @return true to start
     */
    public abstract boolean shouldStart();

    /**
     * Starts this {@link GoalSelector}.
     */
    public abstract void start();

    /**
     * Called every tick when this {@link GoalSelector} is running.
     *
     * @param time the time of the update in milliseconds
     */
    public abstract void tick(long time);

    /**
     * Whether or not this {@link GoalSelector} should end.
     *
     * @return true to end
     */
    public abstract boolean shouldEnd();

    /**
     * Ends this {@link GoalSelector}.
     */
    public abstract void end();

    /**
     * Finds a target based on the entity {@link TargetSelector}.
     *
     * @return the target entity, null if not found
     */
    @Nullable
    public Entity findTarget() {
        for (TargetSelector targetSelector : entityCreature.getTargetSelectors()) {
            final Entity entity = targetSelector.findTarget();
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Finds a target for an entity.
     * If the current one is not present, falls back to {@link GoalSelector#findTarget()}
     * and updates the target inside the entity.
     *
     * @return the target entity, null if not found
     */
    @Nullable
    public Entity findAndUpdateTarget() {
        Entity target = entityCreature.getTarget();
        if (target == null) {
            target = findTarget();
            entityCreature.setTarget(target);
        }
        return target;
    }

    /**
     * Gets the entity behind the goal selector.
     *
     * @return the entity
     */
    @NotNull
    public EntityCreature getEntityCreature() {
        return entityCreature;
    }

    /**
     * Changes the entity affected by the goal selector.
     * <p>
     * WARNING: this does not add the goal selector to {@code entityCreature},
     * this only change the internal entity field. Be sure to remove the goal from
     * the previous entity and add it to the new one using {@link EntityCreature#getGoalSelectors()}.
     *
     * @param entityCreature the new affected entity
     */
    public void setEntityCreature(@NotNull EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }
}
