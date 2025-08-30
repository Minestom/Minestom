package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.DoNothingGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.attribute.Attribute;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(EntityType.CHICKEN);

        GoalSelector.Slot slot = new GoalSelector.Slot();

        getAi()
                .addGoal(new DoNothingGoal(this, 500, 0.5f), slot)
                //.addGoal(new MeleeAttackGoal(this, 3, 2, TimeUnit.MILLISECOND), slot)
                .addGoal(new RandomStrollGoal(this, 10), slot);

        getAi()
                .addTargetSelector(new LastEntityDamagerTarget(this, 15))
                .addTargetSelector(new ClosestEntityTarget(this, 15, LivingEntity.class));

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
    }

    @Override
    public void spawn() {

    }
}
