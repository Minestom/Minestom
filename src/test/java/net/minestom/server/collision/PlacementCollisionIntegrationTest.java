package net.minestom.server.collision;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PlacementCollisionIntegrationTest {

    @Test
    public void empty(Env env) {
        var instance = env.createFlatInstance();
        assertNull(BlockCollision.canPlaceBlockAt(instance, new Vec(0, 40, 0), Block.STONE));
    }

    @Test
    public void entityBlock(Env env) {
        var instance = env.createFlatInstance();
        new Entity(EntityType.ZOMBIE).setInstance(instance, new Pos(0, 40, 0)).join();
        assertNotNull(BlockCollision.canPlaceBlockAt(instance, new Vec(0, 40, 0), Block.STONE));
    }

    @Test
    public void slab(Env env) {
        var instance = env.createFlatInstance();
        new Entity(EntityType.ZOMBIE).setInstance(instance, new Pos(0, 40.75, 0)).join();
        assertNull(BlockCollision.canPlaceBlockAt(instance, new Vec(0, 40, 0), Block.STONE_SLAB));
    }

    @Test
    public void belowPlayer(Env env) {
        var instance = env.createFlatInstance();
        env.createPlayer(instance, new Pos(5.7, -8, 6.389));
        assertNull(BlockCollision.canPlaceBlockAt(instance, new Vec(5, -9, 6), Block.STONE));
    }
}
