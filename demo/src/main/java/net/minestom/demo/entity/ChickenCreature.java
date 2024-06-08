package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.attribute.Attribute;

import java.util.List;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(EntityType.CHICKEN);

        addAIGroup(
                List.of(
//                        new DoNothingGoal(this, 500, 0.1f),
//                        new MeleeAttackGoal(this, 500, 2, TimeUnit.MILLISECOND),
                        new RandomStrollGoal(this, 2)
                ),
                List.of(
//                        new LastEntityDamagerTarget(this, 15),
//                        new ClosestEntityTarget(this, 15, LivingEntity.class)
                )
        );

        // Another way to register previously added EntityAIGroup, using specialized builder:
//        addAIGroup(
//                new EntityAIGroupBuilder()
//                        .addGoalSelector(new DoNothingGoal(this, 500, .1F))
//                        .addGoalSelector(new MeleeAttackGoal(this, 500, 2, TimeUnit.MILLISECOND))
//                        .addGoalSelector(new RandomStrollGoal(this, 2))
//                        .addTargetSelector(new LastEntityDamagerTarget(this, 15))
//                        .addTargetSelector(new ClosestEntityTarget(this, 15, LivingEntity.class))
//                        .build()
//        );

        getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
    }

    @Override
    public void spawn() {

    }
}
