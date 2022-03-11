package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertEquals(res, entity2);
    }

    @Test
    public void entityPhysicsCheckLineOfSightBehind(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(-10, 42, 0)).join();

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertNull(res);
    }

    @Test
    public void entityPhysicsCheckLineOfSightNearMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.31)).join();

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertNull(res);
    }

    @Test
    public void entityPhysicsCheckLineOfSightNearHit(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 0.3)).join();

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertEquals(res, entity2);
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

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertEquals(res, entity3);
    }

    @Test
    public void entityPhysicsCheckLineOfSightBigMiss(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setView(-90, 0);

        var entity2 = new Entity(EntityTypes.ZOMBIE);
        entity2.setInstance(instance, new Pos(10, 42, 10)).join();

        Entity res = entity.getLineOfSightEntity(20, (e) -> true);
        assertNull(res);
    }
}
