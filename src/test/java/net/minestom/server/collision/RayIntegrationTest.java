package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class RayIntegrationTest {

    private static final Vec NORMAL_UP = new Vec(0, 1, 0);
    private static final Vec NORMAL_DOWN = new Vec(0, -1, 0);

    @Test
    public void blockFinderTests(Env env) {
        Instance instance = env.createEmptyInstance();
        instance.setBlock(0, 0, 0, Block.STONE);
        instance.setBlock(0, 1, 0, Block.ANVIL);
        instance.setBlock(0, 2, 0, Block.TORCH);

        Vec origin = new Vec(0.5, 4, 0.5);
        Ray ray = new Ray(origin, new Vec(0, -10, 0));

        BlockFinder blockFinder = ray.findBlocks(instance);
        Ray.Intersection<@NotNull Block> first = blockFinder.nextClosest();
        assertNotNull(first);
        assertEquals(2, first.t());
        assertEquals(new Vec(0.5, 2, 0.5), first.point());
        assertEquals(new Vec(0, 1, 0), first.normal());
        assertEquals(new Vec(0, -1, 0), first.exitNormal());
        assertEquals(Block.ANVIL, first.object());

        assertNotNull(blockFinder.nextClosest());
        assertNull(blockFinder.nextClosest());
        assertFalse(blockFinder.hasNext());

        List<Ray.Intersection<@NotNull Block>> finderHits = new ArrayList<>();
        ray.findBlocks(instance).forEachRemaining(finderHits::addAll);
        assertEquals(List.of(
                new Ray.Intersection<@NotNull Block>(
                        2, origin.withY(2), NORMAL_UP,
                        2 + (11. / 16), origin.withY(2 - (11. / 16)), NORMAL_DOWN,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        2 + (11. / 16), origin.withY(2 - (11. / 16)), NORMAL_UP,
                        2 + (12. / 16), origin.withY(2 - (12. / 16)), NORMAL_DOWN,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        2 + (12. / 16), origin.withY(2 - (12. / 16)), NORMAL_UP,
                        3, origin.withY(1), NORMAL_DOWN,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        3, origin.withY(1), NORMAL_UP,
                        4, origin.withY(0), NORMAL_DOWN,
                        Block.STONE)
        ), finderHits);

        BlockFinder nonSolidFinder = ray.findBlocks(instance, BlockFinder.BLOCK_HITBOXES);
        assertEquals(Block.TORCH, Objects.requireNonNull(nonSolidFinder.nextClosest()).object());
        assertEquals(Block.ANVIL, Objects.requireNonNull(nonSolidFinder.nextClosest()).object());

        Ray.Intersection<@NotNull Block> cubeIntersection = ray.findBlocks(instance, BlockFinder.CUBE_HITBOXES).nextClosest();
        assertEquals(new Ray.Intersection<>(
                1, origin.withY(3), NORMAL_UP,
                2, origin.withY(2), NORMAL_DOWN,
                Block.TORCH
        ), cubeIntersection);

        Ray.Intersection<@NotNull Block> solidCubeIntersection = ray.findBlocks(instance, BlockFinder.SOLID_CUBE_HITBOXES).nextClosest();
        assertNotNull(solidCubeIntersection);
        assertEquals(Block.ANVIL, solidCubeIntersection.object());
        assertEquals(2, solidCubeIntersection.t());
        assertEquals(3, solidCubeIntersection.exitT());
    }

    @Test
    public void blockQueueTests(Env env) {
        Instance instance = env.createEmptyInstance();
        instance.setBlock(new Vec(0, 1, 0), Block.ANVIL);
        instance.setBlock(new Vec(0, 0, 0), Block.STONE);

        Vec origin = new Vec(0.5, 3, 0.5);
        Ray ray = new Ray(origin, new Vec(0, -10, 0));

        BlockQueue blockQueue = ray.blockQueue(instance);
        assertEquals(3, blockQueue.refillSome()); // Anvil returns 3 hits
        assertEquals(1, blockQueue.refill()); // Stone returns 1 hit
        assertEquals(0, blockQueue.refillAll()); // Nothing left to refill
        assertEquals(0, blockQueue.refillSome()); // Returns 0 when appropriate

        assertEquals(2, blockQueue.mergeAll(BlockQueue.SAME_BLOCK_TYPE));
        assertEquals(new Ray.Intersection<@NotNull Block>(
                        1, origin.withY(2), NORMAL_UP,
                        2, origin.withY(1), NORMAL_DOWN,
                        Block.ANVIL
                ), blockQueue.peek()
        );

        assertEquals(1, blockQueue.mergeAll());
        assertEquals(new Ray.Intersection<@NotNull Block>(
                        1, origin.withY(2), NORMAL_UP,
                        3, origin.withY(0), NORMAL_DOWN,
                        Block.ANVIL
                ), blockQueue.poll()
        );
    }

    @Test
    public void entityTests(Env env) {
        Instance instance = env.createEmptyInstance();
        instance.setBlock(new Vec(0, 1, 0), Block.ANVIL);
        Entity entity1 = new Entity(EntityType.WARDEN);
        Entity entity2 = new Entity(EntityType.COD);
        Entity entity3 = new Entity(EntityType.ZOMBIE);
        entity1.setInstance(instance, new Vec(0.5, 2, 0.5));
        entity2.setInstance(instance, new Vec(0.5, 2, 0.5));
        entity3.setInstance(instance, new Vec(1.5, 2, 1.5));

        Vec origin = new Vec(0.5, 3, 0.5);
        Ray ray = new Ray(origin, new Vec(0, -10, 0));

        List<Entity> entities = new ArrayList<>();
        ray.entities(instance.getEntities()).forEach(e -> entities.add(e.object()));
        assertEquals(2, entities.size());
        assertTrue(entities.contains(entity1));
        assertTrue(entities.contains(entity2));

        List<Entity> entitiesSorted = new ArrayList<>();
        ray.entitiesSorted(instance.getEntities()).forEach(e -> entitiesSorted.add(e.object()));
        assertEquals(List.of(entity1, entity2), entitiesSorted);
    }
}