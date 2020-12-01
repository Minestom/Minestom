package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GoalSelector {

    public GoalSelector() {

    }

    /**
     * Whether or not this {@link GoalSelector} should start.
     *
     * @return true to start
     */
    public abstract boolean shouldStart(@NotNull EntityCreature entityCreature);

    /**
     * Starts this {@link GoalSelector}.
     */
    public abstract void start(@NotNull EntityCreature entityCreature);

    /**
     * Called every tick when this {@link GoalSelector} is running.
     *
     * @param time the time of the update in milliseconds
     */
    public abstract void tick(@NotNull EntityCreature entityCreature, long time);

    /**
     * Whether or not this {@link GoalSelector} should end.
     *
     * @return true to end
     */
    public abstract boolean shouldEnd(@NotNull EntityCreature entityCreature);

    /**
     * Ends this {@link GoalSelector}.
     */
    public abstract void end(@NotNull EntityCreature entityCreature);

    /**
     * Finds a target based on the entity {@link TargetSelector}.
     *
     * @return the target entity, null if not found
     */
    @Nullable
    public Entity findTarget(@NotNull EntityCreature entityCreature) {
        for (TargetSelector targetSelector : entityCreature.getTargetSelectors()) {
            final Entity entity = targetSelector.findTarget();
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
