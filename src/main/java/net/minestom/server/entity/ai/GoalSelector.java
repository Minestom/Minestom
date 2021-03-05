package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public abstract class GoalSelector {

    private WeakReference<EntityAIGroup> aiGroupWeakReference;
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
        EntityAIGroup aiGroup = getAIGroup();
        if (aiGroup == null) {
            return null;
        }
        for (TargetSelector targetSelector : aiGroup.getTargetSelectors()) {
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
    @NotNull
    public EntityCreature getEntityCreature() {
        return entityCreature;
    }

    /**
     * Changes the entity affected by the goal selector.
     * <p>
     * WARNING: this does not add the goal selector to {@code entityCreature},
     * this only change the internal entity AI group's field. Be sure to remove the goal from
     * the previous entity AI group and add it to the new one using {@link EntityAIGroup#getGoalSelectors()}.
     *
     * @param entityCreature the new affected entity
     */
    public void setEntityCreature(@NotNull EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    void setAIGroup(@NotNull EntityAIGroup group) {
        this.aiGroupWeakReference = new WeakReference<>(group);
    }

    @Nullable
    protected EntityAIGroup getAIGroup() {
        return this.aiGroupWeakReference.get();
    }

}
