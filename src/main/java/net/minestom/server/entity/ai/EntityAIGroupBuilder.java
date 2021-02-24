package net.minestom.server.entity.ai;

import java.util.ArrayList;
import java.util.List;

public class EntityAIGroupBuilder {

    private final EntityAI ai;
    private final List<GoalSelector> goalSelectors = new ArrayList<>();
    private final List<TargetSelector> targetSelectors = new ArrayList<>();

    EntityAIGroupBuilder(EntityAI ai) {
        this.ai = ai;
    }

    /**
     * Adds {@link GoalSelector} to the list of goal selectors of the building {@link EntityAIGroup}.
     * Addition order is also a priority: priority the higher the earlier selector was added.
     *
     * @param goalSelector goal selector to be added.
     * @return this builder.
     */
    public EntityAIGroupBuilder addGoalSelector(GoalSelector goalSelector) {
        this.goalSelectors.add(goalSelector);
        return this;
    }

    /**
     * Adds {@link TargetSelector} to the list of target selectors of the building {@link EntityAIGroup}.
     * Addition order is also a priority: priority the higher the earlier selector was added.
     *
     * @param targetSelector target selector to be added.
     * @return this builder.
     */
    public EntityAIGroupBuilder addTargetSelector(TargetSelector targetSelector) {
        this.targetSelectors.add(targetSelector);
        return this;
    }

    /**
     * Creates new {@link EntityAIGroup} and adds it to the owning {@link EntityAI} of this builder.
     */
    public void build() {
        this.ai.addAIGroup(this.goalSelectors, this.targetSelectors);
    }

}
