package net.minestom.server.tag;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE)
@Outcome(id = "5", expect = ACCEPTABLE)
@Outcome(id = "null", expect = FORBIDDEN, desc = "Tag is not seen")
@State
public class TagTest {
    private static final Tag<Integer> TAG = Tag.Integer("key");

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        handler.setTag(TAG, 1);
    }

    @Actor
    public void actor2() {
        handler.setTag(TAG, 5);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = handler.getTag(TAG);
    }
}
