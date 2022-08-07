package net.minestom.server.collision;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityBlockClipIntegrationTest {

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

        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(4.7, 42, -10.3), res.newPosition());
    }

    @Test
    public void entityPhysicsCheckEdgeClip(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);

        entity.setInstance(instance, new Pos(0, 42, 0.7)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(10, 0, 0));
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(0.7, 42, 0.7), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1, 42, 1.1), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1.1, 42, 1), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1, 42, 0.7), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1.3, 42, 1), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1, 42, 0.7), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(0.7, 42, 1), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1, 42, 1.3), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(0.7, 42, 1), res.newPosition());
    }

    // Checks CE include checks for crossing two intermediate block (4 block checks)
    @Test
    public void entityPhysicsSmallMoveC0E(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(1, 43, 0, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.52, 42.51, 0.5)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.57, 0.57, 0.57));
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1.09, 43, 1.07), res.newPosition());
    }

    @Test
    public void entityPhysicsSmallMoveC1E(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 43, 1, Block.STONE);

        var entity = new Entity(EntityType.ZOMBIE);
        entity.setBoundingBox(BoundingBox.ZERO);

        entity.setInstance(instance, new Pos(0.50, 42.51, 0.52)).join();
        assertEquals(instance, entity.getInstance());

        PhysicsResult res = CollisionUtils.handlePhysics(entity, new Vec(0.57, 0.57, 0.57));
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1.07, 43, 1.09), res.newPosition());
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
        EntityBlockPhysicsIntegrationTest.assertEqualsPoint(new Pos(1.08, 43, 1.08), res.newPosition());
    }

}
