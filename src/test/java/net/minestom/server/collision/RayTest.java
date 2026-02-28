package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RayTest {

    @Test
    public void simpleRay() {
        Vec origin = new Vec(10, 10, 10);
        Vec direction = new Vec(10, 20, 10);
        Ray ray = new Ray(origin, direction);
        BoundingBox test = new BoundingBox(new Vec(-1, 0, -1), new Vec(1, 2, 1));
        Ray.Intersection<?> intersection = ray.cast(test, new Vec(15, 20, 15));

        assertNotNull(intersection);
        assertEquals(test, intersection.object());
        assertEquals(new Vec(15, 20, 15), intersection.point());
        assertEquals(new Vec(5, 10, 5).length(), intersection.t());
        assertEquals(new Vec(0, -1, 0), intersection.normal());

        Ray.Intersection<@NotNull Ray> expectedEndPoint = new Ray.Intersection<>(
                direction.length(), origin.add(direction), direction.normalize().neg(),
                direction.length(), origin.add(direction), direction.normalize(),
                ray
        );
        assertEquals(expectedEndPoint, ray.endPoint());
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
                Block.STONE
        );
        Ray.Intersection<@NotNull Block> intersection2 = new Ray.Intersection<>(
                2, new Vec(2, 0, 0), new Vec(-1, 0, 0),
                3, new Vec(3, 0, 0), new Vec(1, 0, 0),
                Block.DIRT
        );
        Ray.Intersection<@NotNull Block> intersection3 = new Ray.Intersection<>(
                4, new Vec(4, 0, 0), new Vec(-1, 0, 0),
                5, new Vec(5, 0, 0), new Vec(1, 0, 0),
                Block.WAXED_WEATHERED_CUT_COPPER_STAIRS
        );
        Ray.Intersection<@NotNull Block> intersection4 = new Ray.Intersection<>(
                4, new Vec(4, 0, 0), new Vec(-1, 0, 0),
                6, new Vec(6, 0, 0), new Vec(1, 0, 0),
                Block.WAXED_WEATHERED_CUT_COPPER_STAIRS
        );
        assertTrue(intersection1.compareTo(intersection2) < 0);
        assertTrue(intersection3.compareTo(intersection4) < 0);
        assertTrue(intersection1.overlaps(intersection2));
        assertTrue(intersection2.overlaps(intersection1));
        assertTrue(intersection2.overlaps(intersection2));
        assertFalse(intersection2.overlaps(intersection3));

        Ray.Intersection<@NotNull Block> expectedMerged = new Ray.Intersection<>(
                1, new Vec(1, 0, 0), new Vec(-1, 0, 0),
                3, new Vec(3, 0, 0), new Vec(1, 0, 0),
                Block.STONE
        );
        assertEquals(
                expectedMerged,
                intersection1.merge(intersection2)
        );
        assertEquals(
                expectedMerged.withObject(Block.DIRT),
                intersection2.merge(intersection1)
        );
    }

    @Test
    public void sorting() {
        Ray ray = new Ray(new Vec(0, 0, 0), new Vec(10, 10, 10));
        BoundingBox b1 = new BoundingBox(new Vec(1), new Vec(5));
        BoundingBox b2 = new BoundingBox(new Vec(3), new Vec(6));
        BoundingBox b3 = new BoundingBox(new Vec(2), new Vec(4));
        BoundingBox behindRay = new BoundingBox(new Vec(-2), new Vec(-1));
        BoundingBox afterRay = new BoundingBox(new Vec(11), new Vec(12));
        List<Shape> boundingBoxes = List.of(b1, b2, b3, behindRay, afterRay);

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
}