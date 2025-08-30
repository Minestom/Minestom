package net.minestom.demo.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.goal.*;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(EntityType.CHICKEN);

        GoalSelector.Slot move = new GoalSelector.Slot();
        GoalSelector.Slot look = new GoalSelector.Slot();
        GoalSelector.Slot damage = new GoalSelector.Slot();

        getAi()
                .addGoal(move, new FollowTargetGoal(this, Duration.of(1, TimeUnit.SECOND), 2))
                .addGoal(move, new DoNothingGoal(this, 500, 0.1f))
                .addGoal(damage, new MeleeAttackGoal(this, 3, 2, TimeUnit.MILLISECOND))
                .addGoal(move, new RandomStrollGoal(this, 3))
                .addGoal(look, new RandomLookAroundGoal(this, 10));

        getAi()
                .addTargetSelector(new LastEntityDamagerTarget(this, 15, 200))
                .addTargetSelector(new ClosestEntityTarget(this, 15, 50, entity -> entity instanceof LivingEntity));

        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
    }

    @Override
    public void spawn() {

    }
}
