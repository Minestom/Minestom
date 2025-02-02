package net.minestom.server.entity.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AIGoal implements Comparable<AIGoal> {

    private final List<TargetSelector> targetSelectors;
    protected EntityCreature entityCreature;
    private AIGoalState aiGoalState = AIGoalState.WAITING;
    private final int goalPriority;

    public AIGoal(@NotNull EntityCreature entityCreature, @Nullable List<TargetSelector> targetSelectors, int goalPriority) {
        this.entityCreature = entityCreature;
        if (targetSelectors == null) {
            this.targetSelectors = List.of();
        } else {
            this.targetSelectors = new ArrayList<>();
            this.targetSelectors.addAll(targetSelectors);
        }
        this.goalPriority = goalPriority;
    }

    /**
     * Sets the entity creature this goal and its target selectors will operate on. Intended for internal use
     * @param entityCreature The entity creature
     */
    @ApiStatus.Internal
    public void setEntityCreature(@NotNull EntityCreature entityCreature) {
        this.entityCreature = entityCreature;
    }

    /**
     * Whether this {@link AIGoal} should start.
     *
     * @return true to start
     */
    public abstract boolean shouldStart();

    /**
     * Starts this {@link AIGoal}.
     */
    public void start() {
        aiGoalState = AIGoalState.ACTIVE;
    }

    /**
     * Called every tick when this {@link AIGoal} is running.
     *
     * @param time the time of the update in milliseconds
     */
    public abstract void tick(long time);

    /**
     * Whether this {@link AIGoal} should end.
     *
     * @return true to end
     */
    public abstract boolean shouldEnd();

    /**
     * Ends this {@link AIGoal}.
     */
    public void end() {
        aiGoalState = AIGoalState.ENDED;
    }

    @Override
    public int compareTo(@NotNull AIGoal o) {
        return o.goalPriority - this.goalPriority;
    }

    /**
     * Checks to see if this goal should interrupt the current goal, given the entity creature's current state
     * @return true if this goal should interrupt the current one, false to continue as normal
     */
    public boolean shouldInterrupt() {
        return false;
    }

    /**
     * Finds a target based on the entity {@link TargetSelector}.
     *
     * @return the target entity, null if not found
     */
    @Nullable
    public Entity findTargetEntity() {
        if (targetSelectors.isEmpty()) {
            return null;
        }
        for (TargetSelector targetSelector : targetSelectors) {
            final Entity entity = targetSelector.findTargetEntity(entityCreature);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    @Nullable
    public Pos findTargetPosition() {
        if (targetSelectors.isEmpty()) {
            return null;
        }
        for (TargetSelector targetSelector : targetSelectors) {
            final Pos position = targetSelector.findTargetPosition(entityCreature);
            if (position != null) {
                return position;
            }
        }
        return null;
    }
}
