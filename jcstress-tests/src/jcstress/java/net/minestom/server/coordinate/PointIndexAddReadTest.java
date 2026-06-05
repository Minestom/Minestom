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
@Description("Concurrent add + chunk read yields a consistent snapshot.")
@Outcome(id = "0", expect = ACCEPTABLE, desc = "Reader scanned before add was published")
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Reader scanned after add was published")
@Outcome(id = "2", expect = FORBIDDEN, desc = "Duplicate entry - bucket snapshot corruption")
@State
public class PointIndexAddReadTest {

    private final PointIndex idx = PointIndex.createConcurrent();

    @Actor
    public void adder() {
        idx.add(1, new Vec(0, 64, 0)); // chunk (0, 0)
    }

    @Actor
    public void reader(I_Result r) {
        final int[] count = {0};
        idx.forEachInChunk(0, 0, id -> count[0]++);
        r.r1 = count[0];
    }
}
