package net.minestom.server.entity.ai;

import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public abstract class Goal {

    private WeakReference<GoalSelector> aiGroupWeakReference;
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
     * Gets the entity behind the goal selector.
     *
     * @return the entity
     */
    public EntityCreature getEntityCreature() {
        return entityCreature;
    }

    /**
     * Changes the entity affected by the goal selector.
     * <p>
     * WARNING: this does not add the goal selector to {@code entityCreature},
     * this only change the internal entity AI group's field. Be sure to remove the goal from
     * the previous entity AI group and add it to the new one using {@link GoalSelector#getGoals()}.
     *
     * @param entityCreature the new affected entity
     */
    public void setEntityCreature(EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    void setAIGroup(GoalSelector group) {
        this.aiGroupWeakReference = new WeakReference<>(group);
    }

    @Nullable
    protected GoalSelector getAIGroup() {
        return this.aiGroupWeakReference.get();
    }

}
