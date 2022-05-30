package net.minestom.server.utils.collection;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.LL_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "1, 2", expect = ACCEPTABLE)
@State
public class ObjectArrayTest {
    private final ObjectArray<Integer> array = ObjectArray.concurrent();

    @Actor
    public void actor1() {
        array.set(255, 1);
    }

    @Actor
    public void actor2() {
        array.set(32_000, 2);
    }

    @Arbiter
    public void arbiter(LL_Result r) {
        r.r1 = array.get(255);
        r.r2 = array.get(32_000);
    }
}
