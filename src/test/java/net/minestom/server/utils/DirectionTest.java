package net.minestom.server.utils;

import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class DirectionTest {
    @Test
    public void testAdd() {
        assertEquals(Direction.NORTH, Direction.SOUTH.add(Direction.SOUTH));
        assertEquals(Direction.EAST, Direction.WEST.add(Direction.SOUTH));
        assertEquals(Direction.SOUTH, Direction.EAST.add(Direction.EAST));
        assertEquals(Direction.WEST, Direction.NORTH.add(Direction.WEST));
    }

    @Test
    public void testSubtract() {
        assertEquals(Direction.SOUTH, Direction.NORTH.subtract(Direction.SOUTH));
        assertEquals(Direction.WEST, Direction.EAST.subtract(Direction.SOUTH));
        assertEquals(Direction.EAST, Direction.EAST.subtract(Direction.NORTH));
        assertEquals(Direction.NORTH, Direction.EAST.subtract(Direction.EAST));
    }
}
