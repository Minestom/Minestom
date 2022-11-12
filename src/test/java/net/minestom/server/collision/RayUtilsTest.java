package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RayUtilsTest {
    @Test
    void manyHeightIntersectionChecks() {
        final Pos rayStart = new Pos(0.5, 1.0, 0.5);
        final Vec rayDirection = new Vec(0.273, -0.0784, 0.0);
        final BoundingBox collidableStatic = new BoundingBox(1, 1, 1);
        final Vec staticCollidableOffset = new Vec(1, 0.0, 0.0);

        for(double y = 1; y < 10; y += 0.001D) {
            final BoundingBox moving = new BoundingBox(0.6, y, 0.6);
            assertTrue(RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, collidableStatic,
                    staticCollidableOffset), moving.toString());
        }
    }
}