package demo.entity;

import com.google.common.collect.ImmutableList;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.DoNothingGoal;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.type.animal.EntityChicken;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;

public class ChickenCreature extends EntityChicken {

    public ChickenCreature(Position defaultPosition) {
        super(defaultPosition);

        addAIGroup(
                ImmutableList.of(
//                        new DoNothingGoal(this, 500, 0.1f),
//                        new MeleeAttackGoal(this, 500, 2, TimeUnit.MILLISECOND),
                        new RandomStrollGoal(this, 2)
                ),
                ImmutableList.of(
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

        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1f);

        addEventCallback(EntityAttackEvent.class, event -> {
            //System.out.println("CALL ATTACK");
            LivingEntity entity = (LivingEntity) event.getTarget();
            Vector velocity = getPosition().clone().getDirection().multiply(6);
            velocity.setY(4f);
            entity.damage(DamageType.fromEntity(this), -1);
            entity.setVelocity(velocity);
        });

    }

    @Override
    public void spawn() {

    }
}
