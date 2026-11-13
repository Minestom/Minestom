package net.minestom.server.world.generation;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PositionPredicateTest {

    @Test
    public void offsetWithinBounds() {
        assertDoesNotThrow(() -> new PositionPredicate.Solid(new Vec(16, -16, 16)));
        assertDoesNotThrow(() -> new PositionPredicate.VolumeMatch(new Vec(-16, -16, -16), new Vec(16, 16, 16), new PositionPredicate.True()));
    }

    @Test
    public void offsetOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.Solid(new Vec(17, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.MatchingBlockTag(new Vec(0, -17, 0), Key.key("dirt")));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.InsideWorldBounds(new Vec(0, 0, 17)));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.VolumeMatch(new Vec(-17, 0, 0), Vec.ZERO, new PositionPredicate.True()));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.VolumeMatch(Vec.ZERO, new Vec(0, 17, 0), new PositionPredicate.True()));
    }

    @Test
    public void volumeMatchMinMustNotExceedMax() {
        assertDoesNotThrow(() -> new PositionPredicate.VolumeMatch(new Vec(-1, -1, -1), new Vec(-1, -1, -1), new PositionPredicate.True()));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.VolumeMatch(new Vec(1, 0, 0), Vec.ZERO, new PositionPredicate.True()));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.VolumeMatch(new Vec(0, 1, 0), Vec.ZERO, new PositionPredicate.True()));
        assertThrows(IllegalArgumentException.class, () -> new PositionPredicate.VolumeMatch(new Vec(0, 0, 1), Vec.ZERO, new PositionPredicate.True()));
    }

    @Test
    public void unobstructedOffsetUnbounded() {
        assertDoesNotThrow(() -> new PositionPredicate.Unobstructed(new Vec(40, 0, 0)));
    }
}
