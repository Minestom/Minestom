package net.minestom.server.utils;

import net.minestom.server.utils.position.PositionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionUtilsTest {

    @Test
    public void yaw() {
        float plusX = PositionUtils.getLookYaw(10, 0);
        assertEquals(-90, plusX, 1E-5);

        float plusZ = PositionUtils.getLookYaw(0, 10);
        assertEquals(0, plusZ, 1E-5);

        float minusX = PositionUtils.getLookYaw(-10, 0);
        assertEquals(90, minusX, 1E-5);

        float minusZNegative = PositionUtils.getLookYaw(1E-5, -10);
        if (minusZNegative < -180) fail();
        assertEquals(-180, minusZNegative, 1E-4);

        float minusZPositive = PositionUtils.getLookYaw(-1E-5, -10);
        if (minusZPositive > 180) fail();
        assertEquals(180, minusZPositive, 1E-4);

        float oneThreeFive = PositionUtils.getLookYaw(-5, -5);
        assertEquals(135, oneThreeFive, 1E-5);

        float fortyFive = PositionUtils.getLookYaw(5, 5);
        assertEquals(-45, fortyFive, 1E-5);
    }

    @Test
    public void highPitch() {
        float high = PositionUtils.getLookPitch(0, 999999, 0);
        assertEquals(-90, high, 1E-5);

        float low = PositionUtils.getLookPitch(0, -999999, 0);
        assertEquals(90, low, 1E-5);

        float zero = PositionUtils.getLookPitch(-5, 0, 5);
        assertEquals(0, zero, 1E-5);

        float fortyFive = PositionUtils.getLookPitch(5, 5, 0);
        assertEquals(-45, fortyFive, 1E-5);
    }

}
