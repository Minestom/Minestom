package fr.themode.demo.entity;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.PlayerTarget;
import net.minestom.server.entity.type.EntityChicken;
import net.minestom.server.utils.Position;

public class ChickenCreature extends EntityChicken {

    public ChickenCreature(Position defaultPosition) {
        super(defaultPosition);

        //goalSelectors.add(new DoNothingGoal(this, 500, 0.1f));
        goalSelectors.add(new RandomStrollGoal(this, 2));
        //goalSelectors.add(new FollowTargetGoal(this));


        targetSelectors.add(new PlayerTarget(this, 15));


        setAttribute(Attribute.MOVEMENT_SPEED, 0.1f);

    }

    @Override
    public void spawn() {

    }
}
