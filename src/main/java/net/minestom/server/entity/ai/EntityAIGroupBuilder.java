package net.minestom.server.entity.ai;

import org.jetbrains.annotations.NotNull;

public class EntityAIGroupBuilder {

    private final EntityAIGroup group = new EntityAIGroup();

    /**
     * Adds {@link AIGoal} to the list of goal selectors of the building {@link EntityAIGroup}.
     *
     * @param goalSelector goal selector to be added.
     * @return this builder.
     */
    public EntityAIGroupBuilder addAIGoal(@NotNull AIGoal goalSelector) {
        this.group.getAIGoals().add(goalSelector);
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
