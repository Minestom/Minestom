package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.SweepResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

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

    @Test
    public void entityPhysicsCheckCollision(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        assertEqualsPoint(new Pos(0, 42.5, 0), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckDiagonal(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);
        instance.setBlock(1, 43, 2, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));

        boolean isFirst = checkPoints(new Pos(10, 42, 0.7), res.newPosition());
        boolean isSecond = checkPoints(new Pos(0.7, 42, 10), res.newPosition());

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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
        assertEqualsPoint(new Pos(0.7, 42, 11.69), res.newPosition());
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

        var entity = new Entity(EntityTypes.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.entityMeta;
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

        var entity = new Entity(EntityTypes.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.entityMeta;
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

        var entity = new Entity(EntityTypes.SLIME);
        SlimeMeta meta = (SlimeMeta) entity.entityMeta;
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

        BoundingBox bb = new Entity(EntityTypes.ZOMBIE).getBoundingBox();

        SweepResult sweepResultFinal = new SweepResult(1, 0, 0, 0, null);

        bb.intersectBoxSwept(z1, movement, z2, bb, sweepResultFinal);
        bb.intersectBoxSwept(z1, movement, z3, bb, sweepResultFinal);

        assertEquals(new Pos(11, 0, 0), sweepResultFinal.getCollidedShapePosition());
        assertEquals(sweepResultFinal.getCollidedShape(), bb);
    }

    @Test
    public void entityPhysicsCheckEdgeClip(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckTouchTick(Env env) {
        var instance = env.createFlatInstance();

        Set<Point> positions = new HashSet<>();
        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                assertTrue(positions.add(touch.getBlockPosition()));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        instance.setBlock(0, 42, 0, Block.STONE.withHandler(handler));
        instance.setBlock(0, 42, 1, Block.STONE.withHandler(handler));
        instance.setBlock(0, 43, 1, Block.STONE.withHandler(handler));
        instance.setBlock(0, 43, -1, Block.STONE.withHandler(handler));
        instance.setBlock(1, 42, 1, Block.STONE.withHandler(handler));
        instance.setBlock(1, 42, 0, Block.STONE.withHandler(handler));
        instance.setBlock(0, 42, 10, Block.STONE.withHandler(handler));

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();

        entity.tick(0);

        assertEquals(positions, Set.of(
                new Vec(0, 42, 0),
                new Vec(0, 42, 1),
                new Vec(0, 43, 1)));

        assertEquals(instance, entity.getInstance());
    }

    @Test
    public void entityPhysicsCheckOnGround(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 50, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -20, 0));
        assertTrue(res.isOnGround());
    }

    @Test
    public void entityPhysicsCheckStairTop(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.4, 42.5, 0.9)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -1.2));
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckStairTopSmall(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.4, 42.5, 0.9)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.2));
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNotOnGround(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 50, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -1, 0));
        assertFalse(res.isOnGround());
    }

    @Test
    public void entityPhysicsCheckNotOnGroundHitUp(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 60, 0, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
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

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(11, 0, 10));
        assertEqualsPoint(new Pos(11, 42, 0.7), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveCollide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.6, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.3, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
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

        var entity = new Entity(EntityTypes.ZOMBIE);
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

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.5, 42, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult px = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        PhysicsResult py = CollisionUtils.handlePhysics(entity, new Vec(0, 10, 0));
        PhysicsResult pz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));

        PhysicsResult nx = CollisionUtils.handlePhysics(entity, new Vec(-10, 0, 0));
        PhysicsResult ny = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        PhysicsResult nz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -10));

        assertEqualsPoint(new Pos(0.7, 42, 0.5), px.newPosition());
        assertEqualsPoint(new Pos(0.5, 42.04, 0.5), py.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.7), pz.newPosition());

        assertEqualsPoint(new Pos(0.3, 42, 0.5), nx.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.5), ny.newPosition());
        assertEqualsPoint(new Pos(0.5, 42, 0.3), nz.newPosition());
    }

    @Test
    public void entityPhysicsCheckLargeVelocityMiss(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

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
        var entity = new Entity(EntityTypes.ZOMBIE);

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
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition());
    }
}