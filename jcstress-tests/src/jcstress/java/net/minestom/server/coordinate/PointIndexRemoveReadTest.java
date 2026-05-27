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
@Description("Concurrent remove + read yields 0 or 1, never a corrupt bucket.")
@Outcome(id = "0", expect = ACCEPTABLE, desc = "Reader saw post-remove state")
@Outcome(id = "1", expect = ACCEPTABLE, desc = "Reader saw pre-remove state")
@Outcome(id = "2", expect = FORBIDDEN, desc = "Duplicate entry - bucket snapshot corruption")
@State
public class PointIndexRemoveReadTest {

    private final PointIndex idx = PointIndex.createConcurrent();

    public PointIndexRemoveReadTest() {
        idx.add(1, new Vec(0, 64, 0)); // chunk (0, 0)
    }

    @Actor
    public void remover() {
        idx.remove(1);
    }

    @Actor
    public void reader(I_Result r) {
        final int[] count = {0};
        idx.forEachInChunk(0, 0, id -> count[0]++);
        r.r1 = count[0];
    }
}
