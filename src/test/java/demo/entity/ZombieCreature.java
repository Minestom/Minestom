package demo.entity;

import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.type.monster.EntityZombie;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;

public class ZombieCreature extends EntityZombie {

    public ZombieCreature(Position spawnPosition) {
        super(spawnPosition);
        goalSelectors.add(new MeleeAttackGoal(this, 30, 2, TimeUnit.TICK));

        targetSelectors.add(new LastEntityDamagerTarget(this, 10));
    }
}
