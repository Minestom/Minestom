package net.minestom.server.coordinate;

import net.minestom.server.utils.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PosViewDirectionTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void withLookAtPos() {
        Pos initialPosition = new Pos(0, 40, 0);
        Pos position;

        // look at itself, direction should not change
        position = initialPosition.withLookAt(initialPosition);
        assertEquals(initialPosition.yaw(), position.yaw());
        assertEquals(initialPosition.pitch(), position.pitch());

        position = initialPosition.withLookAt(new Pos(16, 40, 16));
        assertEquals(-45f, position.yaw());
        assertEquals(0f, position.pitch(), EPSILON);

        position = initialPosition.withLookAt(new Pos(-16, 40, 56));
        assertEquals(15.94f, position.yaw(), EPSILON);
        assertEquals(0f, position.pitch(), EPSILON);

        position = initialPosition.withLookAt(new Pos(48, 36, 48));
        assertEquals(-45f, position.yaw(), EPSILON);
        assertEquals(4.76f, position.pitch(), EPSILON);

        position = initialPosition.withLookAt(new Pos(48, 36, -17));
        assertEquals(-109.50f, position.yaw(), EPSILON);
        // should have the same pitch as the previous position
        assertEquals(4.76f, position.pitch(), EPSILON);

        position = initialPosition.withLookAt(new Pos(0, 87, 0));
        // looking from below, not checking the yaw
        assertEquals(-90f, position.pitch(), EPSILON);

        position = initialPosition.withLookAt(new Pos(-25, 42, 4));
        assertEquals(80.90f, position.yaw(), EPSILON);
        assertEquals(-4.57f, position.pitch(), EPSILON);
    }

    /**
     * Testing {@link Pos#facing()}
     */
    @Test
    public void facingTest() {
        Pos pos = new Pos(0, 0, 0);

        assertEquals(Direction.SOUTH, pos.facing());

        assertEquals(Direction.NORTH, pos.withYaw(180 + 360).facing());

        assertEquals(Direction.EAST, pos.withYaw(-90).facing());

        assertEquals(Direction.WEST, pos.withYaw(90).facing());

        assertEquals(Direction.DOWN, pos.withYaw(543210).withPitch(53).facing());

        assertEquals(Direction.UP, pos.withYaw(123456).withPitch(-90).facing());

        // edges
        assertEquals(Direction.SOUTH, pos.withYaw(45).facing());

        assertEquals(Direction.NORTH, pos.withYaw(-135).facing());

        assertEquals(Direction.EAST, pos.withYaw(-45).facing());

        assertEquals(Direction.WEST, pos.withYaw(135).facing());
    }
}
