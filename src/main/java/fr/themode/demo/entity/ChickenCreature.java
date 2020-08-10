package fr.themode.demo.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.type.animal.EntityChicken;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

public class ChickenCreature extends EntityChicken {

    public ChickenCreature(Position defaultPosition) {
        super(defaultPosition);

        //goalSelectors.add(new DoNothingGoal(this, 500, 0.1f));
        //goalSelectors.add(new MeleeAttackGoal(this, 500, TimeUnit.MILLISECOND));
        goalSelectors.add(new RandomStrollGoal(this, 2));
        /*goalSelectors.add(new EatBlockGoal(this,
                new HashMap<>() {
                    {
                        put(Block.GRASS.getBlockId(), Block.AIR.getBlockId());
                    }
                },
                new HashMap<>() {
                    {
                        put(Block.GRASS_BLOCK.getBlockId(), Block.DIRT.getBlockId());
                    }
                },
                100))
        ;
        //goalSelectors.add(new FollowTargetGoal(this));*/


        //targetSelectors.add(new LastEntityDamagerTarget(this, 15));
        //targetSelectors.add(new ClosestEntityTarget(this, 15, LivingEntity.class));


        setAttribute(Attribute.MOVEMENT_SPEED, 0.1f);

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
