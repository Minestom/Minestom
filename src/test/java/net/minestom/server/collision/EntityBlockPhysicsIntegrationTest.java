package net.minestom.server.collision;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityBlockPhysicsIntegrationTest {
    private static final Point PRECISION = new Pos(0.01, 0.01, 0.01);

    private static boolean checkPoints(Point expected, Point actual) {
        Point diff = expected.sub(actual);

        return (PRECISION.x() > Math.abs(diff.x()))
                && (PRECISION.y() > Math.abs(diff.y()))
                && (PRECISION.z() > Math.abs(diff.z()));
    }

    private static void assertEqualsPoint(Point expected, Point actual) {
        assertEquals(expected.x(), actual.x(), PRECISION.x());
        assertEquals(expected.y(), actual.y(), PRECISION.y());
        assertEquals(expected.z(), actual.z(), PRECISION.z());
    }

    private static void assertPossiblePoints(List<Point> expected, Point actual) {
        for (Point point : expected) {
            if (checkPoints(point, actual)) {
                return;
            }
        }

        fail("Expected one of the following points: " + expected);
    }

    @Test
    public void entityPhysicsCheckCollision(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        assertEqualsPoint(new Pos(0, 42.5, 0), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckShallowAngle(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(13, 99, 16, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(12.812, 100.0, 16.498)).join();

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.273, -0.0784, 0.0));
        assertTrue(res.isOnGround());
        assertTrue(res.collisionY());
        assertEqualsPoint(new Vec(13.09, 100, 16.5), res.newPosition());
        assertEqualsPoint(new Vec(0.273, 0, 0), res.newVelocity());
    }

    @Test
    public void entityPhysicsCheckFallFence(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.OAK_FENCE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 43.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -0.25, 0));
        assertEqualsPoint(new Pos(0.5, 43.5, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckFallHitCarpet(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.OAK_FENCE);
        instance.setBlock(0, 43, 0, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 54.0625, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -11.03, 0));
        assertEqualsPoint(new Pos(0, 43.0625, 0), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckFallHitFence(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.OAK_FENCE);
        instance.setBlock(0, 43, 0, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 54.0625, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -11.03, 0));
        assertEqualsPoint(new Pos(0.5, 43.5, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckHorizontalFence(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 0, Block.OAK_FENCE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 43.25, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(2, 0, 0));
        assertEqualsPoint(new Pos(1.075, 43.25, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckHorizontalCarpetedFence(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 0, Block.OAK_FENCE);
        instance.setBlock(1, 43, 0, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 43.25, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(2, 0, 0));
        assertEqualsPoint(new Pos(1.075, 43.25, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonalCarpetedFenceX(Env env) {
        var instance = env.createFlatInstance();

        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        instance.setBlock(1, 42, 0, Block.OAK_FENCE);
        instance.setBlock(1, 43, 0, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.075, 44.0625, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(2, -2, 0));
        assertEqualsPoint(new Pos(1.075, 43.0625, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonalCarpetedFenceZ(Env env) {
        var instance = env.createFlatInstance();

        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        instance.setBlock(0, 42, 1, Block.OAK_FENCE);
        instance.setBlock(0, 43, 1, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 44.0625, 0.075)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -2, 2));
        assertEqualsPoint(new Pos(0.5, 43.0625, 1.075), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonalCarpetedFenceXZ(Env env) {
        var instance = env.createFlatInstance();

        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        instance.setBlock(0, 42, 1, Block.OAK_FENCE.withProperties(Map.of("north", "true", "west", "true")));
        instance.setBlock(0, 42, 0, Block.OAK_FENCE.withProperties(Map.of("south", "true")));
        instance.setBlock(-1, 42, 1, Block.OAK_FENCE.withProperties(Map.of("east", "true")));

        instance.setBlock(0, 43, 1, Block.BROWN_CARPET);
        instance.setBlock(0, 43, 0, Block.BROWN_CARPET);
        instance.setBlock(-1, 43, 1, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(-0.925, 44.0625, 0.075)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(2, -2, 2));
        PhysicsResult res2 = CollisionUtils.handlePhysics(entity, new Vec(5, -5, 2));
        PhysicsResult res3 = CollisionUtils.handlePhysics(entity, new Vec(2, -5, 5));

        Point expected = new Pos(0.075, 43.0625, 1.075);

        assertEqualsPoint(expected, res.newPosition());
        assertEqualsPoint(expected, res2.newPosition());
        assertEqualsPoint(expected, res3.newPosition());
    }

    @Test
    public void entityPhysicsCheckFallHitFenceLongMove(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.OAK_FENCE);
        instance.setBlock(0, 43, 0, Block.BROWN_CARPET);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 54.0625, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -21, 0));
        assertEqualsPoint(new Pos(0.5, 43.5, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckFenceAboveHead(Env env) {
        var instance = env.createFlatInstance();

        instance.setBlock(0, 45, 0, Block.OAK_FENCE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 43.0, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 2, 0));
        assertEqualsPoint(new Pos(0.5, 43.05, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonal(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));

        // First and second are both valid, it depends on the implementation
        assertPossiblePoints(List.of(new Pos(10, 42, 0.7), new Pos(0.7, 42, 10)), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDirectSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.69, 42, 0.69)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
        assertEqualsPoint(new Pos(0.7, 42, 11.69), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckCorner(Env env) {
        var instance = env.createFlatInstance();
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        var entity = new Entity(EntityType.ZOMBIE);

        instance.setBlock(5, 43, -5, Block.STONE);

        entity.setInstance(instance, new Pos(-0.3, 42, -0.3)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, -10));

        assertEqualsPoint(new Pos(4.7, 42, -10.3), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEnclosedHit(Env env) {
        var instance = env.createFlatInstance();
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        instance.setBlock(8, 42, 8, Block.STONE);

        var entity = new Entity(EntityType.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.getEntityMeta();
        meta.setSize(20);

        entity.setInstance(instance, new Pos(5, 50, 5)).join();

        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -20, 0));

        assertEqualsPoint(new Pos(5, 43, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEnclosedHitSubBlock(Env env) {
        var instance = env.createFlatInstance();
        for (int i = -2; i <= 2; ++i)
            for (int j = -2; j <= 2; ++j)
                instance.loadChunk(i, j).join();

        instance.setBlock(8, 42, 8, Block.LANTERN);

        var entity = new Entity(EntityType.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.getEntityMeta();
        meta.setSize(20);

        entity.setInstance(instance, new Pos(5, 42.8, 5)).join();

        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -0.4, 0));

        assertEqualsPoint(new Pos(5, 42.56, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEnclosedMiss(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(11, 43, 11, Block.STONE);

        var entity = new Entity(EntityType.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.getEntityMeta();
        meta.setSize(5);

        entity.setInstance(instance, new Pos(5, 44, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -2, 0));

        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEntityHit(Env env) {
        Point z1 = new Pos(0, 0, 0);
        Point z2 = new Pos(15, 0, 0);
        Point z3 = new Pos(11, 0, 0);
        Point movement = new Pos(20, 1, 0);

        BoundingBox bb = new Entity(EntityType.ZOMBIE).getBoundingBox();

        SweepResult sweepResultFinal = new SweepResult(1, 0, 0, 0, null);

        bb.intersectBoxSwept(z1, movement, z2, bb, sweepResultFinal);
        bb.intersectBoxSwept(z1, movement, z3, bb, sweepResultFinal);

        assertEquals(new Pos(11, 0, 0), sweepResultFinal.collidedShapePosition);
        assertEquals(sweepResultFinal.collidedShape, bb);
    }

    @Test
    public void entityPhysicsCheckEdgeClip(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEdgeClipSmall(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.6999, 42, 0.6999)).join();

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.702, 0, 0.702));

        // First and second are both valid, it depends on the implementation
        assertPossiblePoints(List.of(new Pos(1.402, 42, 0.7), new Pos(0.7, 42, 1.402)), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockNorth(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "north", "open", "true"));

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 0.4));
        assertEqualsPoint(new Pos(0.5, 42.5, 0.512), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockSouth(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "south", "open", "true"));

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.4));
        assertEqualsPoint(new Pos(0.5, 42.5, 0.487), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockWest(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "west", "open", "true"));

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.6, 0, 0));
        assertEqualsPoint(new Pos(0.512, 42.5, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockEast(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "east", "open", "true"));

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(-0.6, 0, 0));
        assertEqualsPoint(new Pos(0.487, 42.5, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockUp(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("half", "top"));

        instance.setBlock(0, 44, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.7, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0.4, 0));
        assertEqualsPoint(new Pos(0.5, 42.862, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockDown(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR;

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.2, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -0.4, 0));
        assertEqualsPoint(new Pos(0.5, 42.187, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckOnGround(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 50, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -20, 0));
        assertTrue(res.isOnGround());
    }

    @Test
    public void entityPhysicsCheckStairTop(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.4, 42.5, 0.9)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -1.2));
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckStairTopSmall(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.4, 42.5, 0.9)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.2));
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNotOnGround(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 50, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -1, 0));
        assertFalse(res.isOnGround());
    }

    @Test
    public void entityPhysicsCheckNotOnGroundHitUp(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 60, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 50, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 20, 0));
        assertFalse(res.isOnGround());
    }

    @Test
    public void entityPhysicsCheckSlide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);
        instance.setBlock(1, 43, 3, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(11, 0, 10));
        assertEqualsPoint(new Pos(11, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveCollide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.6, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.3, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0), res.newPosition());
    }

    // Checks C include all checks for crossing one intermediate block (3 block checks)
    @Test
    public void entityPhysicsSmallMoveC0(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.7, 42, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.6, 0, 0.6));
        assertEqualsPoint(new Pos(1, 42, 1.1), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC1(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.5, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.6, 0, 0.6));
        assertEqualsPoint(new Pos(1.1, 42, 1), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC2(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.8, 42, 1.3)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.6, 0, -0.6));
        assertEqualsPoint(new Pos(1, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC3(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.7, 42, 1.1)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.6, 0, -0.6));
        assertEqualsPoint(new Pos(1.3, 42, 1), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC4(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(1.1, 42, 1.3)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(-0.6, 0, -0.6));
        assertEqualsPoint(new Pos(1, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC5(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(1.3, 42, 1.1)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(-0.6, 0, -0.6));
        assertEqualsPoint(new Pos(0.7, 42, 1), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC6(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(1.1, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(-0.6, 0, 0.6));
        assertEqualsPoint(new Pos(1, 42, 1.3), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC7(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 42, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(1.3, 42, 0.8)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(-0.6, 0, 0.6));
        assertEqualsPoint(new Pos(0.7, 42, 1), res.newPosition());
    }

    // Checks CE include checks for crossing two intermediate block (4 block checks)
    @Test
    public void entityPhysicsSmallMoveC0E(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.51, 42.51, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.57, 0.57, 0.57));
        assertPossiblePoints(List.of(new Pos(1.08, 43, 1.07), new Pos(1.0, 43.08, 1.07)), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC1E(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.50, 42.51, 0.51)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.57, 0.57, 0.57));
        assertPossiblePoints(List.of(new Pos(1.07, 43, 1.08), new Pos(1.07, 43.08, 1.0)), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC2E(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.51, 42.50, 0.51)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.57, 0.57, 0.57));

        assertPossiblePoints(List.of(new Pos(1.0, 43.08, 1.08), new Pos(1.08, 43.0, 1.08), new Pos(1.08, 43.08, 1.0)), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 10), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckBlockMiss(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 2, Block.STONE);
        instance.setBlock(2, 43, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));
        assertEqualsPoint(new Pos(10, 42, 10), res.newPosition());
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

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult px = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        PhysicsResult py = CollisionUtils.handlePhysics(entity, new Vec(0, 10, 0));
        PhysicsResult pz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));

        PhysicsResult nx = CollisionUtils.handlePhysics(entity, new Vec(-10, 0, 0));
        PhysicsResult ny = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        PhysicsResult nz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -10));

        assertEqualsPoint(new Pos(0.7, 42, 0.5), px.newPosition());
        assertEqualsPoint(new Pos(0.5, 42.05, 0.5), py.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.7), pz.newPosition());

        assertEqualsPoint(new Pos(0.3, 42, 0.5), nx.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.5), ny.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.3), nz.newPosition());
    }

    @Test
    public void entityPhysicsCheckLargeVelocityMiss(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        assertEqualsPoint(new Pos((distance - 1) * 16 + 5, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckLargeVelocityHit(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        instance.setBlock(distance * 8, 43, 5, Block.STONE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        assertEqualsPoint(new Pos(distance * 8 - 0.3, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoMove(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsRepeatedCollision(Env env) {
        var instance = env.createFlatInstance();
        PhysicsResult previousResult = null;

        instance.setBlock(0, 41, 0, Block.STONE);

        instance.setBlock(1, 42, 0, Block.STONE);
        instance.setBlock(0, 42, 1, Block.STONE);
        instance.setBlock(0, 42, -1, Block.STONE);
        instance.setBlock(-1, 42, 0, Block.STONE);

        instance.setBlock(1, 43, 0, Block.STONE);
        instance.setBlock(0, 43, 1, Block.STONE);
        instance.setBlock(0, 43, -1, Block.STONE);
        instance.setBlock(-1, 43, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 43.1, 0.5)).join();

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 0));
        entity.teleport(res.newPosition()).join();

        while ((previousResult == null || !previousResult.newPosition().samePoint(res.newPosition())) && entity.getPosition().y() >= 42) {
            previousResult = res;
            res = CollisionUtils.handlePhysics(entity, new Vec(0.1, -0.01, 0));
            entity.teleport(res.newPosition()).join();
        }

        assertEqualsPoint(new Pos(0.7, 42, 0.5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoMoveCache(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, Vec.ZERO, res);
        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoMoveLargeVelocityHit(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        instance.setBlock(distance * 8, 43, 5, Block.STONE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0), res);
        assertEqualsPoint(new Pos(distance * 8 - 0.3, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckLargeVelocityHitNoMove(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);

        final int distance = 20;
        for (int x = 0; x < distance; ++x) instance.loadChunk(x, 0).join();

        instance.setBlock(distance * 8, 43, 5, Block.STONE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, Vec.ZERO, res);
        assertEqualsPoint(new Pos(distance * 8 - 0.3, 42, 5), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDoorSubBlockSouthRepeat(Env env) {
        var instance = env.createFlatInstance();
        Block b = Block.ACACIA_TRAPDOOR.withProperties(Map.of("facing", "south", "open", "true"));

        instance.setBlock(0, 42, 0, b);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42.5, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.4));
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.4), res);

        assertEqualsPoint(new Pos(0.5, 42.5, 0.487), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckCollisionDownCache(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0), res);

        assertEqualsPoint(new Pos(0, 40, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckGravityCached(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        entity.teleport(res.newPosition());
        res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0), res);
        entity.teleport(res.newPosition());

        PhysicsResult lastPhysicsResult;

        for (int x = 0; x < 50; ++x) {
            lastPhysicsResult = res;
            res = CollisionUtils.handlePhysics(entity, new Vec(0, -1.7, 0), res);
            entity.teleport(res.newPosition());

            if (x > 10) assertSame(lastPhysicsResult, res, "Physics result not cached");
        }

        assertEqualsPoint(new Pos(0, 40, 0.7), res.newPosition());
    }
}