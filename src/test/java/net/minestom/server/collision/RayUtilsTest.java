package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RayUtilsTest {
    private static final BoundingBox BOX = new BoundingBox(2, 2, 2);
    private static final Vec START = Vec.ZERO;

    @Test
    public void positiveAndNegativeAxisHits() {
        final double expected = 0.5 * 0.99999;
        assertEquals(expected, percentage(new Vec(4, 0, 0), new Vec(4, 0, 0)));
        assertEquals(expected, percentage(new Vec(-4, 0, 0), new Vec(-4, 0, 0)));
        assertEquals(expected, percentage(new Vec(0, 4, 0), new Vec(0, 4, 0)));
        assertEquals(expected, percentage(new Vec(0, -4, 0), new Vec(0, -4, 0)));
        assertEquals(expected, percentage(new Vec(0, 0, 4), new Vec(0, 0, 4)));
        assertEquals(expected, percentage(new Vec(0, 0, -4), new Vec(0, 0, -4)));
    }

    @Test
    public void parallelRayOutsideSlabMisses() {
        assertTrue(Double.isNaN(percentage(new Vec(4, 0, 0), new Vec(0, 0, 4))));
    }

    @Test
    public void startingOverlapMisses() {
        assertTrue(Double.isNaN(percentage(Vec.ZERO, new Vec(1, 0, 0))));
    }

    @Test
    public void touchingBoxesHitImmediately() {
        assertEquals(0, percentage(new Vec(2, 0, 0), new Vec(1, 0, 0)));
    }

    @Test
    public void maximumPercentageRejectsLaterHit() {
        assertTrue(Double.isNaN(RayUtils.boundingBoxIntersectionPercentage(
                BOX, START, new Vec(4, 0, 0), BOX, new Vec(4, 0, 0), 0.49)));
    }

    @Test
    public void resultWrapperPreservesAxisPriorityAndCollisionDetails() {
        final var result = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);
        assertTrue(BOX.intersectBoxSwept(START, new Vec(4, 0, 4), new Vec(4, 0, 4), BOX, result));

        assertEquals(0.5 * 0.99999, result.res);
        assertEquals(1, result.normalX);
        assertEquals(0, result.normalY);
        assertEquals(0, result.normalZ);
        assertEquals(new Vec(4 * result.res, 0, 4 * result.res),
                new Vec(result.collidedPositionX, result.collidedPositionY, result.collidedPositionZ));
        assertEquals(BOX, result.collidedShape);
    }

    @Test
    public void resultWrapperDoesNotMutateOnMiss() {
        final var result = new SweepResult(0.25, 1, 1, 1, BOX, 1, 2, 3, 4, 5, 6);
        assertFalse(BOX.intersectBoxSwept(START, new Vec(4, 0, 0), new Vec(4, 0, 0), BOX, result));

        assertEquals(0.25, result.res);
        assertEquals(1, result.normalX);
        assertEquals(1, result.normalY);
        assertEquals(1, result.normalZ);
        assertEquals(BOX, result.collidedShape);
    }

    private static double percentage(Vec position, Vec direction) {
        return RayUtils.boundingBoxIntersectionPercentage(
                BOX, START, direction, BOX, position, Double.MAX_VALUE);
    }
}
