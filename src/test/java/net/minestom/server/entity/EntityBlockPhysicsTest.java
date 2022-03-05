package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityBlockPhysicsTest {

    @Test
    public void entityPhysicsCheckCollision(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 1, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEquals(new Pos(0, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        assertEquals(new Pos(0, 42.5, 0), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonal(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);
        instance.setBlock(1, 42, 2, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(11, 0, 10));
        assertEquals(new Pos(0.7, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDirectSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);
        instance.setBlock(1, 42, 2, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0.7, 42, 0.6)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
        assertEquals(new Pos(0.7, 42, 0.6), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);
        instance.setBlock(1, 42, 2, Block.STONE);
        instance.setBlock(1, 42, 3, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(11,  0, 10));
        assertEquals(new Pos(11.77, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEquals(new Pos(0, 42, 10), res.newPosition());
    }
}
