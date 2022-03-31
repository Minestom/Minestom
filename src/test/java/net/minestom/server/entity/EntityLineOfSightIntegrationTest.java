package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityLineOfSightIntegrationTest {
    @Test
    public void entityPhysicsCheckLineOfSight(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0)).join();

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertEquals(entity2, entity.getLineOfSightEntity(20, false, (e) -> true));
        assertNull(entity.getLineOfSightEntity(20, true, (e) -> true));
        assertTrue(entity.isOnLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertFalse(entity.isOnLineOfSight(entity2, true));
        assertFalse(entity.hasLineOfSight(entity2, true));
    }

    @Test
    public void entityPhysicsCheckLineOfSightBehind(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(-10, 42, 0)).join();

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(-5, y, z, Block.STONE);
            }
        }

        assertNull(entity.getLineOfSightEntity(20, false, (e) -> true));
        assertFalse(entity.isOnLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertFalse(entity.hasLineOfSight(entity2, true));
    }

    @Test
    public void entityPhysicsCheckLineOfSightNearMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.31)).join();

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertNull(entity.getLineOfSightEntity(20, false, (e) -> true));
        assertFalse(entity.isOnLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertFalse(entity.hasLineOfSight(entity2, true));
    }

    @Test
    public void entityPhysicsCheckLineOfSightNearHit(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.3)).join();

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertEquals(entity2, entity.getLineOfSightEntity(20, false, (e) -> true));
        assertNull(entity.getLineOfSightEntity(20, true, (e) -> true));
        assertTrue(entity.isOnLineOfSight(entity2, false));
        assertFalse(entity.isOnLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertFalse(entity.hasLineOfSight(entity2, true));
    }

    @Test
    public void entityPhysicsCheckLineOfSightCorrectOrder(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0)).join();

        var entity3 = new Entity(EntityTypes.ZOMBIE);
        entity3.setInstance(instance, new Pos(5, 42, 0)).join();

        assertEquals(entity3, entity.getLineOfSightEntity(20, false, (e) -> true));
        assertEquals(entity3, entity.getLineOfSightEntity(20, true, (e) -> true));
        assertTrue(entity.isOnLineOfSight(entity2, false));
        assertTrue(entity.isOnLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.isOnLineOfSight(entity3, false));
        assertTrue(entity.isOnLineOfSight(entity3, false));
        assertTrue(entity.hasLineOfSight(entity3, false));
        assertTrue(entity.hasLineOfSight(entity3, true));
    }

    @Test
    public void entityPhysicsCheckLineOfSightBigMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 10)).join();

        assertNull(entity.getLineOfSightEntity(20, false, (e) -> true));
        assertFalse(entity.isOnLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity2, true));
    }
}
