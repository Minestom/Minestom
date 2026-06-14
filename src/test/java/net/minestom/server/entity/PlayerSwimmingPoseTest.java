package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnvTest
public class PlayerSwimmingPoseTest {

    @Test
    public void swimWhenSprintingWithEyesInWater(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();

        assertEquals(EntityPose.SWIMMING, player.getPose(), "Player should be SWIMMING when sprinting with eyes in water");
    }

    @Test
    public void noSwimInShallowWater(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim when eyes are above water");
    }

    @Test
    public void noSwimOnDryLand(Env env) {
        var instance = env.createFlatInstance();

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));

        player.setSprinting(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim on dry land");
    }

    @Test
    public void noSwimWithoutSprinting(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(false);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim when not sprinting");
    }

    @Test
    public void stopSwimmingOnLeaveWater(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();
        assertEquals(EntityPose.SWIMMING, player.getPose());

        instance.setBlock(0, 40, 0, Block.AIR);
        instance.setBlock(0, 41, 0, Block.AIR);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should stop swimming after leaving water");
    }

    @Test
    public void swimInWaterloggedBlock(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 41, 0, Block.OAK_SLAB.withProperty("waterlogged", "true"));

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();

        assertEquals(EntityPose.SWIMMING, player.getPose(), "Player should swim with eyes in a waterlogged block");
    }

    @Test
    public void noSwimWhenEyesAboveFlowingWaterSurface(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 41, 0, Block.WATER.withProperty("level", "7"));

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim when eyes are above flowing water surface");
    }

    @Test
    public void stopSwimmingWhenFlying(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();
        assertEquals(EntityPose.SWIMMING, player.getPose());

        player.setFlying(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should be forced out of swimming when flying");
    }

    @Test
    public void stopSwimmingInSpectator(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        env.tick();
        assertEquals(EntityPose.SWIMMING, player.getPose());

        player.setGameMode(GameMode.SPECTATOR);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should be forced out of swimming in spectator mode");
    }

    @Test
    public void noSwimWhenFlying(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        player.setFlying(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim when flying");
    }

    @Test
    public void noSwimInSpectator(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER);
        instance.setBlock(0, 41, 0, Block.WATER);

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40, 0.5));

        player.setSprinting(true);
        player.setGameMode(GameMode.SPECTATOR);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not swim in spectator mode");
    }

    @Test
    public void noSwimWhenFeetAboveShallowWaterSurface(Env env) {
        var instance = env.createFlatInstance();
        instance.setBlock(0, 40, 0, Block.WATER.withProperty("level", "7"));
        instance.setBlock(0, 41, 0, Block.WATER.withProperty("level", "7"));

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0.5, 40.2, 0.5));

        player.setSprinting(true);
        env.tick();

        assertNotEquals(EntityPose.SWIMMING, player.getPose(), "Player should not be in water when feet are above the shallow fluid surface");
    }
}
