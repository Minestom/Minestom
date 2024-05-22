package net.minestom.server.coordinate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PosViewDirectionTest {
    private static final float EPSILON = 0.01f;

    @Test
    void withLookAtPos() {
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
}
