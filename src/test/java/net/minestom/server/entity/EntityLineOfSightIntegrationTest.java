package net.minestom.server.entity;

import net.minestom.testing.Env;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class EntityLineOfSightIntegrationTest {
    @Test
    void entityPhysicsCheckLineOfSight(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0)).join();

        assertEquals(entity2, entity.getLineOfSightEntity(20, (e) -> true));
        assertTrue(entity.hasLineOfSight(entity2, true));

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertNull(entity.getLineOfSightEntity(20, (e) -> true));
        assertFalse(entity.hasLineOfSight(entity2, true));
    }

    @Test
    void entityPhysicsCheckLineOfSightBehind(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(-10, 42, 0)).join();

        assertNull(entity.getLineOfSightEntity(20, (e) -> true));
        assertFalse(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(-5, y, z, Block.STONE);
            }
        }

        assertFalse(entity.hasLineOfSight(entity2, false));
    }

    @Test
    void entityPhysicsCheckLineOfSightNearMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.31)).join();

        assertNull(entity.getLineOfSightEntity(20, (e) -> true));
        assertFalse(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertFalse(entity.hasLineOfSight(entity2, false));
    }

    @Test
    void entityPhysicsCheckLineOfSightNearHit(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.3)).join();

        assertEquals(entity2, entity.getLineOfSightEntity(20, (e) -> true));
        assertTrue(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertNull(entity.getLineOfSightEntity(20, (e) -> true));
        assertFalse(entity.hasLineOfSight(entity2, true));
        assertFalse(entity.hasLineOfSight(entity2, false));
    }

    @Test
    void entityPhysicsCheckLineOfSightCorrectOrder(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0)).join();

        var entity3 = new Entity(EntityTypes.ZOMBIE);
        entity3.setInstance(instance, new Pos(5, 42, 0)).join();

        assertEquals(entity3, entity.getLineOfSightEntity(20, (e) -> true));
        assertTrue(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));
        assertTrue(entity.hasLineOfSight(entity3, true));
        assertTrue(entity.hasLineOfSight(entity3, false));
    }

    @Test
    void entityPhysicsCheckLineOfSightBigMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 10)).join();

        assertNull(entity.getLineOfSightEntity(20, (e) -> true));
        assertFalse(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));
    }
    @Test
    void entityPhysicsCheckLineOfSightLargeBoundingBox(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(6, 42, 0)).join();
        entity2.setBoundingBox(4.0, 2.0, 4.0);

        for (int z = -1; z <= 1; ++z) {
            for (int y = 40; y <= 44; ++y) {
                instance.setBlock(5, y, z, Block.STONE);
            }
        }

        assertEquals(entity2, entity.getLineOfSightEntity(20, (e) -> true));
        assertTrue(entity.hasLineOfSight(entity2, true));
        assertTrue(entity.hasLineOfSight(entity2, false));
    }
}
