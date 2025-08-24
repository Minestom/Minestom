package net.minestom.server.coordinate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static net.minestom.server.coordinate.CoordConversion.hashBlockCoord;
import static net.minestom.server.coordinate.CoordConversion.hashGlobalCoord;
import static org.junit.jupiter.api.Assertions.*;

public class CoordinateHashTest {

    // Test constants
    private static final int COLLISION_TEST_SIZE = 100000;
    private static final int DISTRIBUTION_BINS = 1000;
    private static final double DISTRIBUTION_TOLERANCE = 0.05; // 5% tolerance for uniform distribution

    @Test
    @DisplayName("hashBlockCoord - Basic functionality test")
    public void testhashBlockCoordBasic() {
        // Test basic functionality
        long hash1 = hashBlockCoord(0, 0, 0);
        long hash2 = hashBlockCoord(1, 1, 1);
        long hash3 = hashBlockCoord(-1, -1, -1);

        // Hash should be deterministic
        assertEquals(hash1, hashBlockCoord(0, 0, 0));
        assertEquals(hash2, hashBlockCoord(1, 1, 1));
        assertEquals(hash3, hashBlockCoord(-1, -1, -1));

        // Different inputs should produce different hashes (with high probability)
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertNotEquals(hash2, hash3);
    }

    @Test
    @DisplayName("hashGlobalCoord - Basic functionality test")
    public void testhashGlobalCoordBasic() {
        // Test basic functionality
        long hash1 = hashGlobalCoord(0.0, 0.0, 0.0);
        long hash2 = hashGlobalCoord(1.0, 1.0, 1.0);
        long hash3 = hashGlobalCoord(-1.0, -1.0, -1.0);

        // Hash should be deterministic
        assertEquals(hash1, hashGlobalCoord(0.0, 0.0, 0.0));
        assertEquals(hash2, hashGlobalCoord(1.0, 1.0, 1.0));
        assertEquals(hash3, hashGlobalCoord(-1.0, -1.0, -1.0));

        // Different inputs should produce different hashes (with high probability)
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertNotEquals(hash2, hash3);
    }

    @Test
    @DisplayName("hashBlockCoord - Edge cases and extreme values")
    public void testhashBlockCoordEdgeCases() {
        // Test with maximum and minimum integer values
        long hashMax = hashBlockCoord(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        long hashMin = hashBlockCoord(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        long hashMixed = hashBlockCoord(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);

        // Should not throw exceptions
        assertNotEquals(0L, hashMax);
        assertNotEquals(0L, hashMin);
        assertNotEquals(0L, hashMixed);

        // All should be different
        assertNotEquals(hashMax, hashMin);
        assertNotEquals(hashMax, hashMixed);
        assertNotEquals(hashMin, hashMixed);
    }

    @Test
    @DisplayName("hashGlobalCoord - Edge cases and extreme values")
    public void testhashGlobalCoordEdgeCases() {
        // Test with special double values
        long hashZero = hashGlobalCoord(0.0, 0.0, 0.0);
        long hashNegZero = hashGlobalCoord(-0.0, -0.0, -0.0);
        long hashInf = hashGlobalCoord(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN);
        long hashMax = hashGlobalCoord(Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL);

        // Should not throw exceptions
        assertNotEquals(0L, hashZero);
        assertNotEquals(0L, hashNegZero);
        assertNotEquals(0L, hashInf);
        assertNotEquals(0L, hashMax);

        // Special case: +0.0 and -0.0 should hash differently due to IEEE-754 bit patterns
        assertNotEquals(hashZero, hashNegZero);
    }

    @Test
    @DisplayName("hashBlockCoord - Sensitivity to single coordinate changes")
    public void testhashBlockCoordSensitivity() {
        long baseHash = hashBlockCoord(100, 200, 300);

        // Small changes in each coordinate should produce different hashes
        long xChange = hashBlockCoord(101, 200, 300);
        long yChange = hashBlockCoord(100, 201, 300);
        long zChange = hashBlockCoord(100, 200, 301);

        assertNotEquals(baseHash, xChange);
        assertNotEquals(baseHash, yChange);
        assertNotEquals(baseHash, zChange);

        // All changes should be different from each other
        assertNotEquals(xChange, yChange);
        assertNotEquals(xChange, zChange);
        assertNotEquals(yChange, zChange);
    }

    @Test
    @DisplayName("hashGlobalCoord - Sensitivity to small coordinate changes")
    public void testhashGlobalCoordSensitivity() {
        long baseHash = hashGlobalCoord(100.0, 200.0, 300.0);

        // Small changes in each coordinate should produce different hashes
        long xChange = hashGlobalCoord(100.000001, 200.0, 300.0);
        long yChange = hashGlobalCoord(100.0, 200.000001, 300.0);
        long zChange = hashGlobalCoord(100.0, 200.0, 300.000001);

        assertNotEquals(baseHash, xChange);
        assertNotEquals(baseHash, yChange);
        assertNotEquals(baseHash, zChange);

        // All changes should be different from each other
        assertNotEquals(xChange, yChange);
        assertNotEquals(xChange, zChange);
        assertNotEquals(yChange, zChange);
    }

    @Test
    @DisplayName("hashBlockCoord - Coordinate order independence test")
    public void testhashBlockCoordOrderIndependence() {
        // Different permutations should produce different hashes
        long hash123 = hashBlockCoord(1, 2, 3);
        long hash132 = hashBlockCoord(1, 3, 2);
        long hash213 = hashBlockCoord(2, 1, 3);
        long hash231 = hashBlockCoord(2, 3, 1);
        long hash312 = hashBlockCoord(3, 1, 2);
        long hash321 = hashBlockCoord(3, 2, 1);

        Set<Long> hashes = Set.of(hash123, hash132, hash213, hash231, hash312, hash321);
        assertEquals(6, hashes.size(), "All permutations should produce different hashes");
    }

    @Test
    @DisplayName("hashGlobalCoord - Coordinate order independence test")
    public void testhashGlobalCoordOrderIndependence() {
        // Different permutations should produce different hashes
        long hash123 = hashGlobalCoord(1.0, 2.0, 3.0);
        long hash132 = hashGlobalCoord(1.0, 3.0, 2.0);
        long hash213 = hashGlobalCoord(2.0, 1.0, 3.0);
        long hash231 = hashGlobalCoord(2.0, 3.0, 1.0);
        long hash312 = hashGlobalCoord(3.0, 1.0, 2.0);
        long hash321 = hashGlobalCoord(3.0, 2.0, 1.0);

        Set<Long> hashes = Set.of(hash123, hash132, hash213, hash231, hash312, hash321);
        assertEquals(6, hashes.size(), "All permutations should produce different hashes");
    }

    @Test
    @DisplayName("hashBlockCoord - Collision resistance test")
    public void testhashBlockCoordCollisions() {
        Set<Long> hashes = new HashSet<>();
        int collisions = 0;

        // Test with a large number of sequential coordinates
        for (int i = -1000; i < 1000; i++) {
            for (int j = -1000; j < 1000; j++) {
                for (int k = -1000; k < 1000; k++) {
                    long hash = hashBlockCoord(i, j, k);
                    if (!hashes.add(hash)) {
                        collisions++;
                    }
                    if (hashes.size() > COLLISION_TEST_SIZE) break;
                }
                if (hashes.size() > COLLISION_TEST_SIZE) break;
            }
            if (hashes.size() > COLLISION_TEST_SIZE) break;
        }

        // Collision rate should be very low (< 0.1%)
        double collisionRate = (double) collisions / hashes.size();
        assertTrue(collisionRate < 0.001,
                String.format("Collision rate too high: %.4f%% (expected < 0.1%%)", collisionRate * 100));
    }

    @Test
    @DisplayName("hashGlobalCoord - Collision resistance test")
    public void testhashGlobalCoordCollisions() {
        Set<Long> hashes = new HashSet<>();
        int collisions = 0;
        Random random = new Random(42); // Fixed seed for reproducibility

        // Test with random coordinates
        for (int i = 0; i < COLLISION_TEST_SIZE; i++) {
            double x = random.nextGaussian() * 1000;
            double y = random.nextGaussian() * 1000;
            double z = random.nextGaussian() * 1000;

            long hash = hashGlobalCoord(x, y, z);
            if (!hashes.add(hash)) {
                collisions++;
            }
        }

        // Collision rate should be very low (< 0.1%)
        double collisionRate = (double) collisions / hashes.size();
        assertTrue(collisionRate < 0.001,
                String.format("Collision rate too high: %.4f%% (expected < 0.1%%)", collisionRate * 100));
    }

    @Test
    @DisplayName("hashBlockCoord - Distribution uniformity test")
    public void testhashBlockCoordDistribution() {
        int[] buckets = new int[DISTRIBUTION_BINS];

        // Generate hashes and distribute into buckets
        for (int i = 0; i < COLLISION_TEST_SIZE; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    long hash = hashBlockCoord(i, j, k);
                    int bucket = (int) (Math.abs(hash) % DISTRIBUTION_BINS);
                    buckets[bucket]++;
                    if (i * 100 + j * 10 + k >= COLLISION_TEST_SIZE) return;
                }
            }
        }

        // Check for reasonable distribution
        int expected = COLLISION_TEST_SIZE / DISTRIBUTION_BINS;
        int tolerance = (int) (expected * DISTRIBUTION_TOLERANCE);

        for (int i = 0; i < DISTRIBUTION_BINS; i++) {
            assertTrue(Math.abs(buckets[i] - expected) <= tolerance,
                    String.format("Bucket %d has %d items, expected %d ± %d", i, buckets[i], expected, tolerance));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000000, -1000, -1, 1, 1000, 1000000})
    @DisplayName("hashBlockCoord - Parameterized boundary tests")
    public void testhashBlockCoordBoundaries(int value) {
        // Test various boundary values
        long hash = hashBlockCoord(value, value, value);
        assertNotEquals(0L, hash, "Hash should not be zero for input: " + value);

        // Test with mixed signs
        long hashMixed = hashBlockCoord(value, -value, value);
        assertNotEquals(hash, hashMixed, "Mixed signs should produce different hash: " + value);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0, 0.0",
            "1.0, 1.0, 1.0",
            "-1.0, -1.0, -1.0",
            "3.14159, 2.71828, 1.41421",
            "1e-10, 1e10, 1e-100",
            "0.000001, 0.000002, 0.000003"
    })
    @DisplayName("hashGlobalCoord - Parameterized precision tests")
    public void testhashGlobalCoordPrecision(double x, double y, double z) {
        long hash = hashGlobalCoord(x, y, z);
        assertNotEquals(0L, hash, String.format("Hash should not be zero for input: (%.10f, %.10f, %.10f)", x, y, z));

        // Test consistency
        assertEquals(hash, hashGlobalCoord(x, y, z), "Hash should be consistent");
    }

    @Test
    @DisplayName("hashBlockCoord - Avalanche effect test")
    public void testhashBlockCoordAvalanche() {
        // Test that flipping a single bit in input changes approximately half the output bits
        long hash1 = hashBlockCoord(0, 0, 0);
        long hash2 = hashBlockCoord(1, 0, 0); // Flip lowest bit of x

        int differentBits = Long.bitCount(hash1 ^ hash2);

        // Should change approximately 32 bits (50% of 64 bits)
        assertTrue(differentBits >= 20 && differentBits <= 44,
                String.format("Avalanche effect insufficient: %d bits changed (expected 20-44)", differentBits));
    }

    @Test
    @DisplayName("hashGlobalCoord - Avalanche effect test")
    public void testhashGlobalCoordAvalanche() {
        // Test that small changes in input produce large changes in output
        long hash1 = hashGlobalCoord(1.0, 1.0, 1.0);
        long hash2 = hashGlobalCoord(1.0000000000000002, 1.0, 1.0); // Smallest possible change

        int differentBits = Long.bitCount(hash1 ^ hash2);

        // Should change approximately 32 bits (50% of 64 bits)
        assertTrue(differentBits >= 20 && differentBits <= 44,
                String.format("Avalanche effect insufficient: %d bits changed (expected 20-44)", differentBits));
    }

    @RepeatedTest(100)
    @DisplayName("hashBlockCoord - Randomized stress test")
    public void testhashBlockCoordRandomized() {
        Random random = new Random();

        int x = random.nextInt();
        int y = random.nextInt();
        int z = random.nextInt();

        long hash = hashBlockCoord(x, y, z);

        // Basic sanity checks
        assertNotEquals(0L, hash, "Hash should not be zero");
        assertEquals(hash, hashBlockCoord(x, y, z), "Hash should be deterministic");
    }

    @RepeatedTest(100)
    @DisplayName("hashGlobalCoord - Randomized stress test")
    public void testhashGlobalCoordRandomized() {
        Random random = new Random();

        double x = random.nextGaussian() * 1e10;
        double y = random.nextGaussian() * 1e10;
        double z = random.nextGaussian() * 1e10;

        long hash = hashGlobalCoord(x, y, z);

        // Basic sanity checks
        assertNotEquals(0L, hash, "Hash should not be zero");
        assertEquals(hash, hashGlobalCoord(x, y, z), "Hash should be deterministic");
    }
}
