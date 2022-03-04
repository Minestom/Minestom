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
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEquals(new Pos(0, 42, 10), res.newPosition());
    }
}
