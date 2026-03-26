package net.minestom.server.entity.ai;

import net.minestom.server.entity.EntityCreature;

/**
 * A goal is the main object of Minestoms entity AI.
 * It represents a task or activity that the entity can be doing.
 * <p>
 * The entity's {@link GoalSelector} will manage the active goals using methods like
 * {@link Goal#canStart()}, {@link Goal#shouldEnd()}, {@link Goal#canInterrupt()}, etc.
 * These methods can be overridden by the goal.
 */
public abstract class Goal {

    protected EntityCreature entityCreature;

    public Goal(EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    /**
     * Whether this {@link Goal} can start.
     *
     * @return true to start
     */
    public abstract boolean canStart();

    /**
     * Whether this {@link Goal} should end.
     *
     * @return true to end
     */
    public abstract boolean shouldEnd();

    /**
     * Whether this {@link Goal} can be interrupted and replaced by another goal.
     *
     * @return whether the goal can be interrupted
     */
    public boolean canInterrupt() {
        return true;
    }

    /**
     * Starts this {@link Goal}.
     */
    public abstract void start();

    /**
     * Called every tick when this {@link Goal} is running.
     *
     * @param time the time of the update in milliseconds
     */
    public abstract void tick(long time);

    /**
     * Ends this {@link Goal}.
     */
    public abstract void end();

    /**
     * Gets the entity behind the goal.
     *
     * @return the entity
     */
    public EntityCreature getEntityCreature() {
        return entityCreature;
    }

    /**
     * Changes the entity affected by the goal.
     * <p>
     * WARNING: this does not add the goal to the entity's {@link GoalSelector},
     * this only changes the internal field. Be sure to remove the goal from
     * the previous goal selector and add it to the new one using {@link GoalSelector#getGoals()}.
     *
     * @param entityCreature the new affected entity
     */
    public void setEntityCreature(EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }
}
