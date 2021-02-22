package demo.entity;

import com.google.common.collect.ImmutableList;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.type.monster.EntityZombie;
import net.minestom.server.utils.Position;

import java.util.Collections;

public class ZombieCreature extends EntityZombie {

    public ZombieCreature(Position spawnPosition) {
        super(spawnPosition);
        addAIGroup(
                ImmutableList.of(
                        new RandomLookAroundGoal(this, 20)
                ),
                Collections.emptyList()
        );
    }
}
