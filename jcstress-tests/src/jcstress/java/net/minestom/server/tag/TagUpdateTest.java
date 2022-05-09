package net.minestom.server.tag;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "null", expect = ACCEPTABLE)
@Outcome(id = "3", expect = ACCEPTABLE)
@Outcome(id = "4", expect = ACCEPTABLE)
@Outcome(id = "10", expect = ACCEPTABLE)
@Outcome(id = "11", expect = ACCEPTABLE)
@State
public class TagUpdateTest {
    private static final Tag<Integer> TAG = Tag.Integer("key");

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        handler.updateAndGetTag(TAG, integer -> integer == null ? 3 : integer + 1);
    }

    @Actor
    public void actor2() {
        handler.updateAndGetTag(TAG, integer -> integer == null ? 10 : integer + 1);
    }

    @Actor
    public void arbiter(L_Result r) {
        r.r1 = handler.getTag(TAG);
    }
}

