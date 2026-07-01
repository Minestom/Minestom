package net.minestom.server.coordinate;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Description("Cross-chunk move keeps the entity visible in exactly one chunk to a concurrent reader.")
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Reader saw the entity in exactly one chunk")
@Outcome(id = "0", expect = FORBIDDEN, desc = "Entity briefly invisible - visibility invariant violated")
@Outcome(id = "2", expect = FORBIDDEN, desc = "Entity briefly visible in both chunks - exact-once invariant violated")
@State
public class PointIndexMoveVisibilityTest {

    private final PointIndex idx = PointIndex.createConcurrent();

    public PointIndexMoveVisibilityTest() {
        idx.add(1, new Vec(0, 64, 0)); // chunk (0, 0)
    }

    @Actor
    public void mover() {
        idx.move(1, new Vec(32, 64, 0)); // chunk (2, 0)
    }

    @Actor
    public void reader(I_Result r) {
        final int[] count = {0};
        idx.forEachInChunkRange(new Vec(16, 64, 0), 2, id -> count[0]++);
        r.r1 = count[0];
    }
}
