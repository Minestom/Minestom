package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static net.minestom.server.coordinate.Point.EPSILON;
import static org.junit.jupiter.api.Assertions.*;

public class VecTest {

    @Test
    public void testConstructors() {
        // Test 3-param constructor
        Vec vec1 = new Vec(1.5, 2.5, 3.5);
        assertEquals(1.5, vec1.x());
        assertEquals(2.5, vec1.y());
        assertEquals(3.5, vec1.z());

        // Test 2-param constructor (x, z) - y defaults to 0
        Vec vec2 = new Vec(1.5, 3.5);
        assertEquals(1.5, vec2.x());
        assertEquals(0.0, vec2.y());
        assertEquals(3.5, vec2.z());

        // Test single value constructor
        Vec vec3 = new Vec(5.5);
        assertEquals(5.5, vec3.x());
        assertEquals(5.5, vec3.y());
        assertEquals(5.5, vec3.z());
    }

    @Test
    public void testConstants() {
        assertEquals(0, Vec.ZERO.x());
        assertEquals(0, Vec.ZERO.y());
        assertEquals(0, Vec.ZERO.z());

        assertEquals(1, Vec.ONE.x());
        assertEquals(1, Vec.ONE.y());
        assertEquals(1, Vec.ONE.z());

        assertEquals(Point.SECTION_SIZE, Vec.SECTION.x());
        assertEquals(Point.SECTION_SIZE, Vec.SECTION.y());
        assertEquals(Point.SECTION_SIZE, Vec.SECTION.z());
    }

    @Test
    public void testRotateAroundX() {
        Vec vec = new Vec(0, 1, 0);

        // Rotate 90 degrees (pi/2 radians)
        Vec rotated = vec.rotateAroundX(Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(1, rotated.z(), EPSILON);

        // Rotate 180 degrees
        rotated = vec.rotateAroundX(Math.PI);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(-1, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);

        // Rotate 360 degrees (should return to original)
        rotated = vec.rotateAroundX(2 * Math.PI);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(1, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);
    }

    @Test
    public void testRotateAroundY() {
        Vec vec = new Vec(1, 0, 0);

        // Rotate 90 degrees
        Vec rotated = vec.rotateAroundY(Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(-1, rotated.z(), EPSILON);

        // Rotate 180 degrees
        rotated = vec.rotateAroundY(Math.PI);
        assertEquals(-1, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);

        // Rotate 360 degrees
        rotated = vec.rotateAroundY(2 * Math.PI);
        assertEquals(1, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);
    }

    @Test
    public void testRotateAroundZ() {
        Vec vec = new Vec(1, 0, 0);

        // Rotate 90 degrees
        Vec rotated = vec.rotateAroundZ(Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(1, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);

        // Rotate 180 degrees
        rotated = vec.rotateAroundZ(Math.PI);
        assertEquals(-1, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);

        // Rotate 360 degrees
        rotated = vec.rotateAroundZ(2 * Math.PI);
        assertEquals(1, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(0, rotated.z(), EPSILON);
    }

    @Test
    public void testRotate() {
        Vec vec = new Vec(1, 0, 0);

        // Rotate around all axes
        Vec rotated = vec.rotate(Math.PI / 2, Math.PI / 2, Math.PI / 2);
        assertNotNull(rotated);

        // Verify it's a combination of individual rotations
        Vec expected = vec.rotateAroundX(Math.PI / 2)
                .rotateAroundY(Math.PI / 2)
                .rotateAroundZ(Math.PI / 2);
        assertEquals(expected.x(), rotated.x(), EPSILON);
        assertEquals(expected.y(), rotated.y(), EPSILON);
        assertEquals(expected.z(), rotated.z(), EPSILON);
    }

    @Test
    public void testRotateFromView() {
        Vec vec = new Vec(1, 0, 0);

        // Test rotation with yaw and pitch
        Vec rotated = vec.rotateFromView(0f, 0f);
        assertNotNull(rotated);

        // Test with 90 degree yaw
        rotated = vec.rotateFromView(90f, 0f);
        assertNotNull(rotated);

        // Test with pitch
        rotated = vec.rotateFromView(0f, 45f);
        assertNotNull(rotated);

        // Test with both
        rotated = vec.rotateFromView(45f, 30f);
        assertNotNull(rotated);
    }

    @Test
    public void testRotateFromViewWithPos() {
        Vec vec = new Vec(1, 0, 0);
        Pos pos = new Pos(0, 0, 0, 45f, 30f);

        Vec rotated = vec.rotateFromView(pos);
        Vec expected = vec.rotateFromView(45f, 30f);

        assertEquals(expected.x(), rotated.x(), EPSILON);
        assertEquals(expected.y(), rotated.y(), EPSILON);
        assertEquals(expected.z(), rotated.z(), EPSILON);
    }

    @Test
    public void testRotateAroundAxis() {
        Vec vec = new Vec(1, 0, 0);
        Vec axis = new Vec(0, 1, 0); // Y-axis

        // Rotate 90 degrees around Y-axis
        Vec rotated = vec.rotateAroundAxis(axis, Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(-1, rotated.z(), EPSILON);

        // Test with non-unit axis (should normalize automatically)
        Vec nonUnitAxis = new Vec(0, 2, 0);
        rotated = vec.rotateAroundAxis(nonUnitAxis, Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(-1, rotated.z(), EPSILON);
    }

    @Test
    public void testRotateAroundNonUnitAxis() {
        Vec vec = new Vec(1, 0, 0);
        Vec axis = new Vec(0, 1, 0);

        Vec rotated = vec.rotateAroundNonUnitAxis(axis, Math.PI / 2);
        assertEquals(0, rotated.x(), EPSILON);
        assertEquals(0, rotated.y(), EPSILON);
        assertEquals(-1, rotated.z(), EPSILON);

        // Test with scaled axis - result should be scaled
        Vec scaledAxis = new Vec(0, 2, 0);
        Vec scaledRotated = vec.rotateAroundNonUnitAxis(scaledAxis, Math.PI / 2);
        // Length should be different from normalized version
        assertNotEquals(rotated.length(), scaledRotated.length(), EPSILON);
    }

    @Test
    public void testNormalize() {
        Vec vec = new Vec(3, 4, 0);
        Vec normalized = vec.normalize();

        // Length should be 1
        assertEquals(1.0, normalized.length(), EPSILON);

        // Direction should be preserved
        assertEquals(0.6, normalized.x(), EPSILON);
        assertEquals(0.8, normalized.y(), EPSILON);
        assertEquals(0, normalized.z(), EPSILON);

        // Already normalized vector
        Vec unit = new Vec(1, 0, 0);
        Vec normalizedUnit = unit.normalize();
        assertEquals(1.0, normalizedUnit.length(), EPSILON);
    }

    @Test
    public void testIsNormalized() {
        Vec unit = new Vec(1, 0, 0);
        assertTrue(unit.isNormalized());

        Vec normalized = new Vec(3, 4, 0).normalize();
        assertTrue(normalized.isNormalized());

        Vec notNormalized = new Vec(2, 0, 0);
        assertFalse(notNormalized.isNormalized());

        Vec zero = Vec.ZERO;
        assertFalse(zero.isNormalized());
    }

    @Test
    public void testLength() {
        Vec vec = new Vec(3, 4, 0);
        assertEquals(5.0, vec.length(), EPSILON);

        Vec vec2 = new Vec(1, 1, 1);
        assertEquals(Math.sqrt(3), vec2.length(), EPSILON);

        assertEquals(0, Vec.ZERO.length(), EPSILON);
    }

    @Test
    public void testLengthSquared() {
        Vec vec = new Vec(3, 4, 0);
        assertEquals(25.0, vec.lengthSquared(), EPSILON);

        Vec vec2 = new Vec(1, 1, 1);
        assertEquals(3.0, vec2.lengthSquared(), EPSILON);

        assertEquals(0, Vec.ZERO.lengthSquared(), EPSILON);
    }

    @Test
    public void testDot() {
        Vec v1 = new Vec(1, 2, 3);
        Vec v2 = new Vec(4, 5, 6);

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0, v1.dot(v2), EPSILON);

        // Perpendicular vectors
        Vec v3 = new Vec(1, 0, 0);
        Vec v4 = new Vec(0, 1, 0);
        assertEquals(0.0, v3.dot(v4), EPSILON);

        // Parallel vectors
        Vec v5 = new Vec(2, 0, 0);
        assertEquals(2.0, v3.dot(v5), EPSILON);
    }

    @Test
    public void testAngle() {
        Vec v1 = new Vec(1, 0, 0);
        Vec v2 = new Vec(0, 1, 0);

        // 90 degrees = pi/2 radians
        assertEquals(Math.PI / 2, v1.angle(v2), EPSILON);

        // 180 degrees
        Vec v3 = new Vec(-1, 0, 0);
        assertEquals(Math.PI, v1.angle(v3), EPSILON);

        // 0 degrees (same direction)
        Vec v4 = new Vec(2, 0, 0);
        assertEquals(0, v1.angle(v4), EPSILON);
    }

    @Test
    public void testCross() {
        Vec v1 = new Vec(1, 0, 0);
        Vec v2 = new Vec(0, 1, 0);

        Vec cross = v1.cross(v2);
        assertEquals(0, cross.x(), EPSILON);
        assertEquals(0, cross.y(), EPSILON);
        assertEquals(1, cross.z(), EPSILON);

        // Reverse order should negate
        Vec crossReverse = v2.cross(v1);
        assertEquals(0, crossReverse.x(), EPSILON);
        assertEquals(0, crossReverse.y(), EPSILON);
        assertEquals(-1, crossReverse.z(), EPSILON);

        // More complex
        Vec v3 = new Vec(2, 3, 4);
        Vec v4 = new Vec(5, 6, 7);
        Vec result = v3.cross(v4);
        assertEquals(-3, result.x(), EPSILON);
        assertEquals(6, result.y(), EPSILON);
        assertEquals(-3, result.z(), EPSILON);
    }

    @Test
    public void testLerp() {
        Vec start = new Vec(0, 0, 0);
        Vec end = new Vec(10, 10, 10);

        // Halfway
        Vec mid = start.lerp(end, 0.5);
        assertEquals(5, mid.x(), EPSILON);
        assertEquals(5, mid.y(), EPSILON);
        assertEquals(5, mid.z(), EPSILON);

        // Start
        Vec atStart = start.lerp(end, 0);
        assertEquals(0, atStart.x(), EPSILON);
        assertEquals(0, atStart.y(), EPSILON);
        assertEquals(0, atStart.z(), EPSILON);

        // End
        Vec atEnd = start.lerp(end, 1);
        assertEquals(10, atEnd.x(), EPSILON);
        assertEquals(10, atEnd.y(), EPSILON);
        assertEquals(10, atEnd.z(), EPSILON);
    }

    @Test
    public void testLerpWithEasing() {
        Vec start = new Vec(0, 0, 0);
        Vec end = new Vec(10, 10, 10);

        // Test with LINEAR easing
        Vec linear = start.lerp(end, 0.5, Point.Easing.LINEAR);
        assertEquals(5, linear.x(), EPSILON);

        // Test with SMOOTH easing
        Vec smooth = start.lerp(end, 0.5, Point.Easing.SMOOTH);
        assertNotNull(smooth);

        // Test with SQUARED_IN easing
        Vec squareIn = start.lerp(end, 0.5, Point.Easing.SQUARED_IN);
        assertNotNull(squareIn);

        // Test with SQUARED_OUT easing
        Vec squareOut = start.lerp(end, 0.5, Point.Easing.SQUARED_OUT);
        assertNotNull(squareOut);

        // Test with SINE easing
        Vec sine = start.lerp(end, 0.5, Point.Easing.SINE);
        assertNotNull(sine);
    }

    @Test
    public void testApply() {
        Vec vec = new Vec(1, 2, 3);

        // Test operator
        Vec result = vec.apply((x, y, z) -> new Vec(x * 2, y * 2, z * 2));
        assertEquals(2, result.x(), EPSILON);
        assertEquals(4, result.y(), EPSILON);
        assertEquals(6, result.z(), EPSILON);

        // Test with predefined operators
        Vec epsilon = new Vec(0.0000001, 0.0000001, 0.0000001);
        Vec epsilonResult = epsilon.apply(Vec.Operator.EPSILON);
        assertEquals(0, epsilonResult.x(), EPSILON);
        assertEquals(0, epsilonResult.y(), EPSILON);
        assertEquals(0, epsilonResult.z(), EPSILON);

        // Test FLOOR
        Vec decimal = new Vec(1.7, 2.3, 3.9);
        Vec floored = decimal.apply(Vec.Operator.FLOOR);
        assertEquals(1, floored.x(), EPSILON);
        assertEquals(2, floored.y(), EPSILON);
        assertEquals(3, floored.z(), EPSILON);

        // Test CEIL
        Vec ceiled = decimal.apply(Vec.Operator.CEIL);
        assertEquals(2, ceiled.x(), EPSILON);
        assertEquals(3, ceiled.y(), EPSILON);
        assertEquals(4, ceiled.z(), EPSILON);

        // Test SIGNUM
        Vec signed = new Vec(-5, 0, 5);
        Vec signum = signed.apply(Vec.Operator.SIGNUM);
        assertEquals(-1, signum.x(), EPSILON);
        assertEquals(0, signum.y(), EPSILON);
        assertEquals(1, signum.z(), EPSILON);
    }

    @Test
    public void testAsVec() {
        Point vec = new Vec(1, 2, 3);
        assertSame(vec, vec.asVec());
    }

    @Test
    public void testDistance() {
        Vec v1 = new Vec(0, 0, 0);
        Vec v2 = new Vec(3, 4, 0);

        assertEquals(5.0, v1.distance(v2), EPSILON);
        assertEquals(5.0, v2.distance(v1), EPSILON);

        // Distance to self
        assertEquals(0, v1.distance(v1), EPSILON);
    }

    @Test
    public void testDistanceSquared() {
        Vec v1 = new Vec(0, 0, 0);
        Vec v2 = new Vec(3, 4, 0);

        assertEquals(25.0, v1.distanceSquared(v2), EPSILON);
        assertEquals(25.0, v2.distanceSquared(v1), EPSILON);
    }

    @Test
    public void testIsZero() {
        assertTrue(Vec.ZERO.isZero());
        assertFalse(Vec.ONE.isZero());
        assertFalse(new Vec(EPSILON, 0, 0).isZero());
        assertFalse(new Vec(0, EPSILON, 0).isZero());
        assertFalse(new Vec(0, 0, EPSILON).isZero());
    }

    @Test
    public void testRandomVectors() {
        Random random = new Random(54321L);

        for (int i = 0; i < 100; i++) {
            double x = random.nextDouble() * 200 - 100;
            double y = random.nextDouble() * 200 - 100;
            double z = random.nextDouble() * 200 - 100;

            Vec vec = new Vec(x, y, z);
            assertEquals(x, vec.x(), EPSILON);
            assertEquals(y, vec.y(), EPSILON);
            assertEquals(z, vec.z(), EPSILON);

            // Test immutability
            Vec original = vec;
            Vec modified = vec.add(1, 2, 3);
            assertEquals(x, original.x(), EPSILON);
            assertEquals(x + 1, modified.x(), EPSILON);

            // Test length consistency
            double length = vec.length();
            double lengthFromSquared = Math.sqrt(vec.lengthSquared());
            assertEquals(length, lengthFromSquared, EPSILON);
        }
    }
}

