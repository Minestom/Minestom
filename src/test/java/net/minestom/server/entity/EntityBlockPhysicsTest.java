package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityBlockPhysicsTest {
    private double roundThreeDecimals(double d) {
        return Math.round(d * 1000) / 1000.0;
    }

    private Point roundPoint(Point p) {
        return new Pos(roundThreeDecimals(p.x()), roundThreeDecimals(p.y()), roundThreeDecimals(p.z()));
    }

    @Test
    public void entityPhysicsCheckCollision(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 1, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEquals(new Pos(0, 42, 0.7), roundPoint(res.newPosition()));
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        assertEquals(new Pos(0, 42.5, 0), roundPoint(res.newPosition()));
    }

    @Test
    public void entityPhysicsCheckDiagonal(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);
        instance.setBlock(1, 42, 2, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));
        Point finalPoint = roundPoint(res.newPosition());

        boolean isFirst = finalPoint.samePoint(new Pos(10.7, 42, 0.7));
        boolean isSecond = finalPoint.samePoint(new Pos(0.7, 42, 10.7));

        // First and second are both valid, it depends on the implementation
        // If x collision is checked first then isFirst will be true
        // If z collision is checked first then isSecond will be true
        assertTrue(isFirst || isSecond);
    }

    @Test
    public void entityPhysicsCheckDirectSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);
        instance.setBlock(1, 42, 2, Block.STONE);
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0.69, 42, 0.69)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
        assertEquals(new Pos(0.7, 42, 11.701), roundPoint(res.newPosition()));
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
        assertEquals(new Pos(11.77, 42, 0.7), roundPoint(res.newPosition()));
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEquals(new Pos(0, 42, 10), roundPoint(res.newPosition()));
    }
}
