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
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class EntityBlockPhysicsIntegrationTest {
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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
        assertEqualsPoint(new Pos(0, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckSlab(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.STONE_SLAB);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 44, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));

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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 11));
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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, -10));

        assertEqualsPoint(new Pos(4.7, 42, -10.3), res.newPosition(), precision);
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

        assertEqualsPoint(new Pos(5, 43, 5), res.newPosition(), precision);
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

        assertEqualsPoint(new Pos(5, 42.56, 5), res.newPosition(), precision);
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

        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckEntityHit(Env env) {
        Point z1 = new Pos(0, 0, 0);
        Point z2 = new Pos(15, 0, 0);
        Point z3 = new Pos(11, 0, 0);
        Point movement = new Pos(20, 1, 0);

        BoundingBox bb = new Entity(EntityTypes.ZOMBIE).getBoundingBox();

        SweepResult sweepResultTemp = new SweepResult(1, 0, 0, 0);
        SweepResult sweepResultFinal = new SweepResult(1, 0, 0, 0);

        bb.intersectBoxSwept(z1, movement, z2, bb, sweepResultTemp, sweepResultFinal);
        bb.intersectBoxSwept(z1, movement, z3, bb, sweepResultTemp, sweepResultFinal);
    }

    @Test
    public void entityPhysicsCheckEdgeClip(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckTouchTick(Env env) {
        var instance = env.createFlatInstance();

        var handler = new BlockHandler() {
            @Override
            public void onTouch(@NotNull Touch touch) {
                System.out.println(touch.getBlockPosition());
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

        // These points should be touched
        // Vec[x=0.0, y=42.0, z=0.0]
        // Vec[x=0.0, y=42.0, z=1.0]
        // Vec[x=0.0, y=43.0, z=1.0]

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();

        entity.tick(0);

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
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckStairTopSmall(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 42, 0, Block.ACACIA_STAIRS);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.4, 42.5, 0.9)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -0.2));
        assertEqualsPoint(new Pos(0.4, 42.5, 0.8), res.newPosition(), precision);
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
        assertEqualsPoint(new Pos(11, 42, 0.7), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsSmallMoveCollide(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0.6, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.3, 0, 0));
        assertEqualsPoint(new Pos(0.7, 42, 0), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckNoCollision(Env env) {
        var instance = env.createFlatInstance();

        var entity = new Entity(EntityTypes.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));
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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 10));
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

        PhysicsResult px = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        PhysicsResult py = CollisionUtils.handlePhysics(entity, new Vec(0, 10, 0));
        PhysicsResult pz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, 10));

        PhysicsResult nx = CollisionUtils.handlePhysics(entity, new Vec(-10, 0, 0));
        PhysicsResult ny = CollisionUtils.handlePhysics(entity, new Vec(0, -10, 0));
        PhysicsResult nz = CollisionUtils.handlePhysics(entity, new Vec(0, 0, -10));

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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
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

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec((distance - 1) * 16, 0, 0));
        assertEqualsPoint(new Pos(distance * 8 - 0.3, 42, 5), res.newPosition(), precision);
    }

    @Test
    public void entityPhysicsCheckNoMove(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityTypes.ZOMBIE);

        entity.setInstance(instance, new Pos(5, 42, 5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, Vec.ZERO);
        assertEqualsPoint(new Pos(5, 42, 5), res.newPosition(), precision);
    }
}