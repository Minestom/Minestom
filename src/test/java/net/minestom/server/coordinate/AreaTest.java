package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AreaTest {

    @Test
    public void cuboidArea() {
        Area.Cuboid area = Area.cuboid(new Vec(1, 2, 3), new Vec(4, 5, 6));
        assertPoint(new Vec(1, 2, 3), area.min());
        assertPoint(new Vec(4, 5, 6), area.max());
    }

    @Test
    public void sectionArea() {
        Area.Cuboid section = Area.section(0, 0, 0);
        assertPoint(new Vec(0, 0, 0), section.min());
        assertPoint(new Vec(15, 15, 15), section.max());
    }

    @Test
    public void sphereArea() {
        Area.Sphere sphere = Area.sphere(new Vec(0, 0, 0), 5);
        assertPoint(new Vec(0, 0, 0), sphere.center());
        assertEquals(5, sphere.radius());
    }
}
