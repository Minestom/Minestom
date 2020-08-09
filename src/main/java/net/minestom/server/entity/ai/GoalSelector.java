package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;

public abstract class GoalSelector {

    protected EntityCreature entityCreature;

    public GoalSelector(EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    /**
     * Whether or not this {@link GoalSelector} should start.
     *
     * @return true to start
     */
    public abstract boolean shouldStart();

    /**
     * Start this {@link GoalSelector}
     */
    public abstract void start();

    /**
     * Called every tick when this {@link GoalSelector} is running
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
     * End this {@link GoalSelector}
     */
    public abstract void end();

    /**
     * Find a target based on the entity {@link TargetSelector}
     *
     * @return the target entity, null if not found
     */
    public Entity findTarget() {
        for (TargetSelector targetSelector : entityCreature.getTargetSelectors()) {
            final Entity entity = targetSelector.findTarget();
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
