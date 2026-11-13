package net.minestom.server.world.generation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NormalNoiseTest {

    @Test
    public void withinBounds() {
        assertDoesNotThrow(() -> new NormalNoise(1.0E-5F, -32, 1, true, List.of()));
        assertDoesNotThrow(() -> new NormalNoise(1.0E6, 32, 32, false, List.of()));
        assertDoesNotThrow(() -> new NormalNoise(1.0, 0, 2, true, List.of(0.0, 1.0E6)));
    }

    @Test
    public void outOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(0, 0, 1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0E6 + 1, 0, 1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, -33, 1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, 33, 1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, 0, 0, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, 0, 33, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, 0, 1, true, List.of(-1.0)));
    }

    @Test
    public void amplitudeModifiersMatchOctaveCount() {
        assertThrows(IllegalArgumentException.class, () -> new NormalNoise(1.0, 0, 1, true, List.of(1.0, 1.0)));
    }
}
