package net.minestom.server.entity.ai;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class ClosestEntityTargetTest {

    @Test
    public void validFindTarget(Env env) {
        var instance = env.createFlatInstance();

        var self = new EntityCreature(EntityType.ZOMBIE);
        self.setInstance(instance, new Pos(0, 42, 0)).join();

        var spider = new EntityCreature(EntityType.SPIDER);
        spider.setInstance(instance, new Pos(-3, 42, -3)).join();

        var secondSpider = new EntityCreature(EntityType.SPIDER);
        secondSpider.setInstance(instance, new Pos(-4, 42, -4)).join();

        var skeleton = new EntityCreature(EntityType.SKELETON);
        skeleton.setInstance(instance, new Pos(5, 42, 5)).join();

        var zombie = new EntityCreature(EntityType.ZOMBIE);
        zombie.setInstance(instance, new Pos(10, 42, -10)).join();

        assertEquals(5, instance.getEntities().size(), "Not all entities are in the instance");

        assertNull(
                new ClosestEntityTarget(1, e -> true).findTargetEntity(self),
                "Entity targets it self"
        );

        assertEquals(spider,
                new ClosestEntityTarget(20, e -> e.getEntityType() == EntityType.SPIDER).findTargetEntity(self),
                "The closest spider was not selected"
        );

        assertNull(
                new ClosestEntityTarget(2, e -> e.getEntityType() == EntityType.SPIDER).findTargetEntity(self),
                "Range distance is not being considered"
        );

        zombie.remove();

        assertNull(
                new ClosestEntityTarget(20, e -> e.getEntityType() == EntityType.ZOMBIE).findTargetEntity(self),
                "Removed entities are included in target selection"
        );

    }

}
