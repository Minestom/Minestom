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
    private static final Point precision = new Pos(0.01, 0.01, 0.01);

    private static boolean checkPoints(Point expected, Point actual, Point delta) {
        Point diff = expected.sub(actual);

        return (delta.x() > Math.abs(diff.x()))
                && (delta.y() > Math.abs(diff.y()))
                && (delta.z() > Math.abs(diff.z()));
    }

    private static void assertEqualsPoint(Point expected, Point actual, Point delta) {
        assertEquals(expected.x(), actual.x(), delta.x());
        assertEquals(expected.y(), actual.y(), delta.y());
        assertEquals(expected.z(), actual.z(), delta.z());
    }

    @Test
    public void entityPhysicsCheckCollision(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        assertEqualsPoint(new Pos(0, 42.5, 0), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckDiagonal(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));

        boolean isFirst = checkPoints(new Pos(10, 42, 0.7), res.newPosition(), precision);
        boolean isSecond = checkPoints(new Pos(0.7, 42, 10), res.newPosition(), precision);

        // First and second are both valid, it depends on the implementation
        // If x collision is checked first then isFirst will be true
        // If z collision is checked first then isSecond will be true
        assertTrue(isFirst || isSecond);
    }

    @Test
    public void entityPhysicsCheckDirectSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.69, 42, 0.69)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
        assertEqualsPoint(new Pos(0.7, 42, 11.69), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckCorner(Env env) {
        var instance = env.createFlatInstance();
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        var entity = new Entity(EntityTypes.ZOMBIE);

        instance.setBlock(5, 43, -5, Block.STONE);

        entity.setInstance(instance, new Pos(-0.3, 42, -0.3)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, -10));

        assertEqualsPoint(new Pos(4.7, 42, -10.3), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckEdgeClip(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);
        instance.setBlock(1, 43, 3, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(11,  0, 10));
        assertEqualsPoint(new Pos(11, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 10), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckBlockMiss(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 2, Block.STONE);
        instance.setBlock(2, 43, 0, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));
        assertEqualsPoint(new Pos(10, 42, 10), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckBlockDirections(Env env) {
        var instance = env.createFlatInstance();

        instance.setBlock(0, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 0, Block.STONE);

        instance.setBlock(0, 43, -1, Block.STONE);
        instance.setBlock(-1, 43, 0, Block.STONE);

        instance.setBlock(0, 41, 0, Block.STONE);
        instance.setBlock(0, 44, 0, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult px = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        CollisionUtils.PhysicsResult py = CollisionUtils.handlePhysics(entity, new Vec(0, 10, 0));
        CollisionUtils.PhysicsResult pz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));

        CollisionUtils.PhysicsResult nx = CollisionUtils.handlePhysics(entity, new Vec(-10, 0, 0));
        CollisionUtils.PhysicsResult ny = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        CollisionUtils.PhysicsResult nz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -10));

        assertEqualsPoint(new Pos(0.7, 42, 0.5), px.newPosition(), precision);
        assertEqualsPoint(new Pos(0.5, 42.04, 0.5), py.newPosition(), precision);
        assertEqualsPoint(new Pos(0.5, 42, 0.7), pz.newPosition(), precision);

        assertEqualsPoint(new Pos(0.3, 42, 0.5), nx.newPosition(), precision);
        assertEqualsPoint(new Pos(0.5, 42, 0.5), ny.newPosition(), precision);
        assertEqualsPoint(new Pos(0.5, 42, 0.3), nz.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckLargeVelocityMiss(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        assertEqualsPoint(new Pos((distance - 1) * 16 + 5, 42, 5), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckLargeVelocityHit(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        instance.setBlock(distance * 8, 43, 5, Block.STONE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        assertEqualsPoint(new Pos(distance * 8 - 0.3, 42, 5), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckNoMove(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        CollisionUtils.PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition(), precision);
    }
}