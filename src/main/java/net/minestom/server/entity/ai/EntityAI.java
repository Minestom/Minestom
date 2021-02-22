package net.minestom.server.entity.ai;

import java.util.Collection;
import java.util.List;

/**
 * Represents an entity which can contain
 * {@link GoalSelector goal selectors} and {@link TargetSelector target selectors}.
 * <p>
 * Both types of selectors are being stored in {@link EntityAIGroup AI groups}.
 * For every group there could be only a single {@link GoalSelector goal selector} running at a time,
 * but multiple groups are independent of each other, so each of them can have own goal selector running.
 */
public interface EntityAI {

    /**
     * Gets the AI groups of this entity.
     *
     * @return a modifiable collection of AI groups of this entity.
     */
    Collection<EntityAIGroup> getAIGroups();

    /**
     * Adds new AI group to this entity.
     *
     * @param group a group to be added.
     */
    default void addAIGroup(EntityAIGroup group) {
        getAIGroups().add(group);
    }

    /**
     * Adds new AI group to this entity, consisting of the given
     * {@link GoalSelector goal selectors} and {@link TargetSelector target selectors}.
     * Their order is also a priority: the lower element index is, the higher priority is.
     *
     * @param goalSelectors   goal selectors of the group.
     * @param targetSelectors target selectors of the group.
     */
    default void addAIGroup(List<GoalSelector> goalSelectors, List<TargetSelector> targetSelectors) {
        EntityAIGroup group = new EntityAIGroup();
        group.getGoalSelectors().addAll(goalSelectors);
        group.getTargetSelectors().addAll(targetSelectors);
        addAIGroup(group);
    }

    default void aiTick(long time) {
        getAIGroups().forEach(group -> group.tick(time));
    }

}
