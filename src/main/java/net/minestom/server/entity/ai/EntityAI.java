package net.minestom.server.entity.ai;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an entity which can contain
 * {@link AIGoal goal selectors} and {@link TargetSelector target selectors}.
 * <p>
 * Both types of selectors are being stored in {@link EntityAIGroup AI groups}.
 * For every group there could be only a single {@link AIGoal goal selector} running at a time,
 * but multiple groups are independent of each other, so each of them can have own goal selector running.
 */
public interface EntityAI {

    /**
     * Returns the AI Group attached to this entity
     * @return the AI Group
     */
    @NotNull EntityAIGroup getAIGroup();

    /**
     * Sets the AI group of this entity
     */
    void setAIGroup(@NotNull EntityAIGroup aiGroup);

    default void addGoals(@NotNull List<AIGoal> aiGoals) {
        getAIGroup().getAIGoals().addAll(aiGoals);
    }

    default void aiTick(long time) {
        getAIGroup().tick(time);
    }
}
