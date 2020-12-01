package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GoalSelector {

    protected EntityCreature entityCreature;

    public GoalSelector(@Nullable EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    public GoalSelector() {
        this.entityCreature = null;
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
     * Gets the entity behind the goal selector.
     *
     * @return the entity
     */
    @Nullable
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
     * WARNING: If the EntityCreature is null when goals are running, the goal will not run properly.
     * Setting it to null is an unsafe operation.
     *
     * @param entityCreature the new affected entity
     */
    public void setEntityCreature(@Nullable EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }
}
