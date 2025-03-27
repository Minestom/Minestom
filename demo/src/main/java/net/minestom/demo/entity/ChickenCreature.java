package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.EntityAIGroup;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.attribute.Attribute;

import java.util.List;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(EntityType.CHICKEN);

        EntityAIGroup aiGroup = new EntityAIGroup();
        aiGroup.addAIGoals(List.of(new RandomStrollGoal(this, 2)));

        setAIGroup(aiGroup);

        //getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
    }

    @Override
    public void spawn() {

    }
}
