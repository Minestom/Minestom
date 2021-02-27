package net.minestom.server.entity.ai;

public class EntityAIGroupBuilder {

    private final EntityAIGroup group = new EntityAIGroup();

    /**
     * Adds {@link GoalSelector} to the list of goal selectors of the building {@link EntityAIGroup}.
     * Addition order is also a priority: priority the higher the earlier selector was added.
     *
     * @param goalSelector goal selector to be added.
     * @return this builder.
     */
    public EntityAIGroupBuilder addGoalSelector(GoalSelector goalSelector) {
        this.group.getGoalSelectors().add(goalSelector);
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
        this.group.getTargetSelectors().add(targetSelector);
        return this;
    }

    /**
     * Creates new {@link EntityAIGroup}.
     *
     * @return new {@link EntityAIGroup}.
     */
    public EntityAIGroup build() {
        return this.group;
    }

}
