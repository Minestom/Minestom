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

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
class RayTest {

    @Test
    public void simpleRay() {
        Ray ray = new Ray(new Vec(10, 10, 10), new Vec(10, 20, 10));
        BoundingBox test = new BoundingBox(new Vec(-1, 0, -1), new Vec(1, 2, 1));
        Ray.Intersection<?> intersection = ray.cast(test, new Vec(15, 20, 15));

        assertNotNull(intersection);
        assertEquals(test, intersection.object());
        assertEquals(new Vec(15, 20, 15), intersection.point());
        assertEquals(new Vec(5, 10, 5).length(), intersection.t());
        assertEquals(new Vec(0, -1, 0), intersection.normal());
    }

    @Test
    public void zeroComponent() {
        Ray ray = new Ray(new Vec(0, 10, 0), new Vec(0, -10, 0));
        BoundingBox b1 = new BoundingBox(1, 1, 1, new Vec(-0.5, 4.5, -0.5));
        BoundingBox b2 = new BoundingBox(1, 1, 1, new Vec(10, 4.5, -0.5));
        assertNotNull(ray.cast(b1));
        assertNull(ray.cast(b2));
    }

    @Test
    public void merging() {
        Ray.Intersection<@NotNull Block> intersection1 = new Ray.Intersection<>(
                1, new Vec(1, 0, 0), new Vec(-1, 0, 0),
                2, new Vec(2, 0, 0), new Vec(1, 0, 0),
                Block.AIR
        );
        Ray.Intersection<@NotNull Block> intersection2 = new Ray.Intersection<>(
                2, new Vec(2, 0, 0), new Vec(-1, 0, 0),
                3, new Vec(3, 0, 0), new Vec(1, 0, 0),
                Block.STONE
        );
        Ray.Intersection<@NotNull Block> intersection3 = new Ray.Intersection<>(
                4, new Vec(4, 0, 0), new Vec(-1, 0, 0),
                5, new Vec(5, 0, 0), new Vec(1, 0, 0),
                Block.WAXED_WEATHERED_CUT_COPPER_STAIRS
        );
        assertTrue(intersection1.compareTo(intersection2) < 0);
        assertTrue(intersection1.overlaps(intersection2));
        assertFalse(intersection2.overlaps(intersection3));

        Ray.Intersection<@NotNull Block> expectedMerged = new Ray.Intersection<>(
                1, new Vec(1, 0, 0), new Vec(-1, 0, 0),
                3, new Vec(3, 0, 0), new Vec(1, 0, 0),
                Block.AIR
        );
        assertEquals(expectedMerged, intersection1.merge(intersection2));
    }

    @Test
    public void sorting() {
        Ray ray = new Ray(new Vec(0, 0, 0), new Vec(10, 10, 10));
        BoundingBox b1 = new BoundingBox(new Vec(1), new Vec(5));
        BoundingBox b2 = new BoundingBox(new Vec(3), new Vec(6));
        BoundingBox b3 = new BoundingBox(new Vec(2), new Vec(4));
        BoundingBox b4 = new BoundingBox(new Vec(-2), new Vec(-1));
        List<Shape> boundingBoxes = List.of(b1, b2, b3, b4);

        Ray.Intersection<@NotNull Shape> first = ray.findFirst(boundingBoxes);
        List<Ray.Intersection<@NotNull Shape>> unsorted = ray.cast(boundingBoxes);
        List<Ray.Intersection<@NotNull Shape>> sorted = ray.castSorted(boundingBoxes);

        assertNotNull(first);
        assertEquals(b1, first.object());
        assertEquals(3, unsorted.size());
        assertEquals(
                List.of(b1, b2, b3),
                List.of(unsorted.get(0).object(), unsorted.get(1).object(), unsorted.get(2).object())
        );
        assertEquals(3, sorted.size());
        assertEquals(
                List.of(b1, b3, b2),
                List.of(sorted.get(0).object(), sorted.get(1).object(), sorted.get(2).object())
        );
    }

    @Test
    public void envTests(Env env) {
        Instance instance = env.createEmptyInstance();
        instance.setBlock(new Vec(0, 1, 0), Block.ANVIL);
        instance.setBlock(new Vec(0, 0, 0), Block.STONE);
        Entity entity1 = new Entity(EntityType.WARDEN);
        Entity entity2 = new Entity(EntityType.COD);
        Entity entity3 = new Entity(EntityType.ZOMBIE);
        entity1.setInstance(instance, new Vec(0.5, 2, 0.5));
        entity2.setInstance(instance, new Vec(0.5, 2, 0.5));
        entity3.setInstance(instance, new Vec(1.5, 2, 1.5));

        Vec origin = new Vec(0.5, 3, 0.5);
        Ray ray = new Ray(origin, new Vec(0, -10, 0));
        Vec normalUp = new Vec(0, 1, 0);
        Vec normalDown = new Vec(0, -1, 0);

        Ray.Intersection<@NotNull Block> first = ray.findBlocks(instance).nextClosest();
        assertNotNull(first);
        assertEquals(1, first.t());
        assertEquals(new Vec(0.5, 2, 0.5), first.point());
        assertEquals(new Vec(0, 1, 0), first.normal());
        assertEquals(new Vec(0, -1, 0), first.exitNormal());
        assertEquals(Block.ANVIL, first.object());

        List<Ray.Intersection<@NotNull Block>> finderHits = new ArrayList<>();
        ray.findBlocks(instance).forEachRemaining(finderHits::addAll);
        assertEquals(List.of(
                new Ray.Intersection<@NotNull Block>(
                        1, origin.withY(2), normalUp,
                        1 + (11. / 16), origin.withY(2 - (11. / 16)), normalDown,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        1 + (11. / 16), origin.withY(2 - (11. / 16)), normalUp,
                        1 + (12. / 16), origin.withY(2 - (12. / 16)), normalDown,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        1 + (12. / 16), origin.withY(2 - (12. / 16)), normalUp,
                        2, origin.withY(1), normalDown,
                        Block.ANVIL),
                new Ray.Intersection<@NotNull Block>(
                        2, origin.withY(1), normalUp,
                        3, origin.withY(0), normalDown,
                        Block.STONE)
        ), finderHits);

        BlockQueue blockQueue = ray.blockQueue(instance);
        assertEquals(3, blockQueue.refillSome());
        assertEquals(1, blockQueue.refill());
        assertEquals(0, blockQueue.refillAll());
        assertEquals(3, blockQueue.mergeAll());
        assertEquals(
                new Ray.Intersection<@NotNull Block>(1, origin.withY(2), normalUp, 3, origin.withY(0), normalDown, Block.ANVIL),
                blockQueue.poll()
        );

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