package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minestom.testing.TestUtils.assertPoint;
import static org.junit.jupiter.api.Assertions.*;

public class AreaTest {

    @Test
    public void lineArea() {
        Area.Line line = Area.line(new Vec(0, 0, 0), new Vec(3, 0, 0));
        Set<Vec> actual = new HashSet<>();
        for (Vec v : line) actual.add(v);
        Set<Vec> expected = Set.of(
                new Vec(0, 0, 0), new Vec(1, 0, 0), new Vec(2, 0, 0), new Vec(3, 0, 0)
        );
        assertEquals(expected, actual);

        // Diagonal line
        Area.Line diag = Area.line(new Vec(0, 0, 0), new Vec(2, 2, 2));
        actual.clear();
        for (Vec v : diag) actual.add(v);
        expected = Set.of(
                new Vec(0, 0, 0), new Vec(1, 1, 1), new Vec(2, 2, 2)
        );
        assertEquals(expected, actual);
    }

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

    @Test
    public void meshArea() {
        // Test simple triangle mesh with varying Y coordinates
        List<Point> triangleVertices = List.of(
                new Vec(0, 5, 0),
                new Vec(3, 5, 0),
                new Vec(1, 5, 3)
        );
        Area.Mesh triangle = Area.mesh(triangleVertices);
        assertEquals(triangleVertices, triangle.vertices());

        // Test that the mesh contains expected points
        Set<Vec> actual = new HashSet<>();
        for (Vec v : triangle) {
            actual.add(v);
        }

        // Triangle should contain point (1, 5, 1) which is inside
        assertTrue(actual.contains(new Vec(1, 5, 1)));
        // Check that y coordinate matches the vertices
        for (Vec point : actual) {
            assertEquals(5, point.blockY());
        }
    }

    @Test
    public void meshSquareArea() {
        // Test square mesh with 3D vertices
        List<Point> squareVertices = List.of(
                new Vec(0, 0, 0),
                new Vec(2, 0, 0),
                new Vec(2, 1, 2),
                new Vec(0, 1, 2)
        );
        Area.Mesh square = Area.mesh(squareVertices);

        Set<Vec> actual = new HashSet<>();
        for (Vec v : square) {
            actual.add(v);
        }

        // Square should contain interior points at various Y levels
        // All points should have Y coordinates between 0 and 1 (from vertices)
        for (Vec point : actual) {
            assertTrue(point.blockY() >= 0 && point.blockY() <= 1,
                    "Y coordinate should be between 0 and 1, got: " + point.blockY());
        }

        // Check that we have reasonable number of points
        assertFalse(actual.isEmpty()); // At minimum, should have some interior points
    }
}
