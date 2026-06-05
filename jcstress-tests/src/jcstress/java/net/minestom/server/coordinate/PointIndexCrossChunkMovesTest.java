package net.minestom.server.coordinate;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Description("Cross-chunk moves of two ids over the same chunk pair complete without deadlock and leave both entries intact.")
@Outcome(id = "2", expect = ACCEPTABLE, desc = "Both moves completed, two ids tracked")
@Outcome(id = "0", expect = FORBIDDEN)
@Outcome(id = "1", expect = FORBIDDEN, desc = "An entry was lost during concurrent moves")
@State
public class PointIndexCrossChunkMovesTest {

    private final PointIndex idx = PointIndex.createConcurrent();
    private static final Vec CHUNK_A = new Vec(0, 64, 0);    // chunk (0, 0)
    private static final Vec CHUNK_B = new Vec(32, 64, 0);   // chunk (2, 0)

    public PointIndexCrossChunkMovesTest() {
        idx.add(1, CHUNK_A);
        idx.add(2, CHUNK_B);
    }

    @Actor
    public void mover1() {
        idx.move(1, CHUNK_B);
    }

    @Actor
    public void mover2() {
        idx.move(2, CHUNK_A);
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = idx.size();
    }
}
