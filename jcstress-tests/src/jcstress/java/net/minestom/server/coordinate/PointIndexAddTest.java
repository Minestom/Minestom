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
@Description("Concurrent adds of different ids to the same chunk preserve both entries.")
@Outcome(id = "2", expect = ACCEPTABLE, desc = "Both adds completed and are visible")
@Outcome(id = "0", expect = FORBIDDEN)
@Outcome(id = "1", expect = FORBIDDEN, desc = "One add was lost - concurrency bug")
@State
public class PointIndexAddTest {

    private final PointIndex idx = PointIndex.createConcurrent();

    @Actor
    public void actor1() {
        idx.add(1, new Vec(0, 64, 0)); // chunk (0, 0)
    }

    @Actor
    public void actor2() {
        idx.add(2, new Vec(4, 64, 4)); // chunk (0, 0)
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = idx.size();
    }
}
