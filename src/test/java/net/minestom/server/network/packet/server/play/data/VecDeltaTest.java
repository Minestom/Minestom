package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VecDeltaTest {

    @Test
    public void emptyStepsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new VecDelta.Stepped(List.of()));
    }

    @Test
    public void emptyPathRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PositionPath.Stepped(List.of()));
    }

    @Test
    public void mismatchedPathEndRejected() {
        // The end position is not written, so a mismatch would silently change on the way through the wire.
        final List<PositionPath.Step> steps = List.of(new PositionPath.Step(new Vec(1, 2, 3), 0));
        assertThrows(IllegalArgumentException.class, () -> new PositionPath.Stepped(new Vec(4, 5, 6), steps));
    }

    @Test
    public void pathEndDerivedFromLastStep() {
        final PositionPath.Stepped path = new PositionPath.Stepped(List.of(
                new PositionPath.Step(new Vec(1, 2, 3), 0),
                new PositionPath.Step(new Vec(4, 5, 6), 2)));
        assertEquals(new Vec(4, 5, 6), path.endPosition());
        assertEquals(path, new PositionPath.Stepped(new Vec(4, 5, 6), path.steps()));
    }
}
