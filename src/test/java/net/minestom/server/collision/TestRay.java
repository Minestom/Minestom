package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class TestRay
{
    @Test
    public void axisAlignedLines(Env env) {
        // Straight axis aligned line (i.e. Vec(1, 0, 0) require special handling
        // to ensure they process correctly

        var instance = env.createFlatInstance();
        instance.loadChunk(-1, 0).join();
        instance.loadChunk(0, -1).join();

        instance.setBlock(0, 52, 0, Block.DIRT);
        instance.setBlock(1, 52, 0, Block.DIRT);

        Ray ray = new Ray(new Vec(-1, 52, 0), new Vec(1, 0, 0), 10);
        assertEquals(2, ray.cast(instance).blockCollisions().size());
        assertEquals(0, ray.withDirection(new Vec(-1, 0, 0)).cast(instance).blockCollisions().size());

        ray = new Ray(new Vec(0, 52, 1), new Vec(0, 0, -1), 10);
        assertEquals(1, ray.cast(instance).blockCollisions().size());
        assertEquals(0, ray.withDirection(new Vec(0, 0, 1)).cast(instance).blockCollisions().size());

        ray = new Ray(new Vec(0, 53, 0), new Vec(0, -1, 0), 10);
        assertEquals(1, ray.cast(instance).blockCollisions().size());
        assertEquals(0, ray.withDirection(new Vec(0, 1, 1)).cast(instance).blockCollisions().size());
    }

    @Test
    public void intersectionDistance(Env env) {
        var instance = env.createFlatInstance();
        Ray ray = new Ray(new Vec(0, 42.01, 0), new Vec(0, -1, 0), 10);
        ray.cast(instance).blockCollisions().forEach(collision -> {
            assertTrue(collision.entry().distance(ray.origin()) <= 10);
        });
    }

    @Test
    public void blockLimit(Env env) {
        var instance = env.createFlatInstance();
        Ray ray = new Ray(new Vec(0, 42, 0), new Vec(0, -1, 1), 20, config -> {
            config.blockCollisionLimit(8);
        });
        assertEquals(8, ray.cast(instance).blockCollisions().size());
    }

    @Test
    public void entityBoundingBoxExpansion(Env env) {
        var instance = env.createFlatInstance();
        Entity entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Vec(0, 42, 0));

        Ray ray = new Ray(new Vec(-11, 42, 0), new Vec(1, 0, 0), 10);
        assertFalse(ray.cast(List.of(entity)).hasEntityCollision());
        assertTrue(ray.withConfiguration(config -> config.entityBoundingBoxExpansion(2, 2, 2)).cast(List.of(entity)).hasEntityCollision());
    }

    @Test
    public void findBeforeCollision(Env env) {
        var instance = env.createFlatInstance();
        Vec direction = new Vec(1, 0, 1).normalize();
        Point origin = new Vec(0, 50, 0);
        Ray ray = new Ray(origin.add(0, 0.05, 0), direction, 14);

        // Make a diagonal line of entities and dirt
        instance.setBlock(origin.add(direction.mul(2)), Block.DIRT);

        Entity entity1 = new Entity(EntityType.ZOMBIE);
        entity1.setInstance(instance, origin.add(direction.mul(4)));

        instance.setBlock(origin.add(direction.mul(6)), Block.DIRT);

        Entity entity2 = new Entity(EntityType.ZOMBIE);
        entity2.setInstance(instance, origin.add(direction.mul(8)));

        instance.setBlock(origin.add(direction.mul(10)), Block.DIRT);

        Entity entity3 = new Entity(EntityType.ZOMBIE);
        entity3.setInstance(instance, origin.add(direction.mul(12)));

        assertEquals(0, ray.cast(instance, List.of(entity1, entity2, entity3)).findEntitiesBeforeBlockCollision().size());
        assertEquals(1, ray.cast(instance, List.of(entity1, entity2, entity3)).findEntitiesBeforeBlockCollision(2).size());
        assertEquals(2, ray.cast(instance, List.of(entity1, entity2, entity3)).findEntitiesBeforeBlockCollision(3).size());

        assertEquals(1, ray.cast(instance, List.of(entity1, entity2, entity3)).findBlocksBeforeEntityCollision().size());
        assertEquals(2, ray.cast(instance, List.of(entity1, entity2, entity3)).findBlocksBeforeEntityCollision(2).size());
        assertEquals(3, ray.cast(instance, List.of(entity1, entity2, entity3)).findBlocksBeforeEntityCollision(3).size());
    }

    @Test
    public void castFromInsideBoundingBox(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(new Vec(1, 10, 1)).join();

        Ray ray = new Ray(new Vec(1, 10, 1), new Vec(1, 2, 1.4), 2);
        assertEquals(ray.cast(instance).firstBlockCollision().entry(), ray.origin());

        Entity entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, ray.origin());
        assertEquals(ray.cast(List.of(entity)).firstEntityCollision().entry(), ray.origin());
    }
}
