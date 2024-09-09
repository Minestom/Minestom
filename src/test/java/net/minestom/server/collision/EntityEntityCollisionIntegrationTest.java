package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityEntityCollisionIntegrationTest {
    @Test
    public void entitySingleCollisionTest(Env env) {
        var instance = env.createFlatInstance();

        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        var movingEntity = new Entity(EntityType.ZOMBIE);
        var stillEntity = new Entity(EntityType.ZOMBIE);
        var doNotHitEntity = new Entity(EntityType.ZOMBIE);

        movingEntity.setInstance(instance, new Vec(0, 42, 0)).join();
        stillEntity.setInstance(instance, new Vec(0, 42, 1)).join();
        doNotHitEntity.setInstance(instance, new Vec(0, 42, 2)).join();

        var result = CollisionUtils.checkEntityCollisions(movingEntity, new Vec(0, 0, 1), 1.51, entity -> entity != movingEntity, null);

        assertEquals(1, result.size());
        assertEquals(stillEntity, result.iterator().next().entity());
    }

    @Test
    public void entityMultipleCollisionTest(Env env) {
        var instance = env.createFlatInstance();

        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        var movingEntity = new Entity(EntityType.ZOMBIE);
        var stillEntity = new Entity(EntityType.ZOMBIE);
        var stillEntity2 = new Entity(EntityType.ZOMBIE);
        var doNotHitEntity = new Entity(EntityType.ZOMBIE);

        movingEntity.setInstance(instance, new Vec(0, 42, 0)).join();
        stillEntity.setInstance(instance, new Vec(0, 42, 1)).join();
        stillEntity2.setInstance(instance, new Vec(0, 42, 2)).join();
        doNotHitEntity.setInstance(instance, new Vec(0, 42, 3)).join();

        var result = CollisionUtils.checkEntityCollisions(movingEntity, new Vec(0, 0, 2), 1.51, entity -> entity != movingEntity, null);

        assertEquals(2, result.size());
    }
}
