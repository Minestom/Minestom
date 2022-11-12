package net.minestom.server.tag;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "2000", expect = ACCEPTABLE)
@State
public class TagUpdateTest {
    private static final Tag<Integer> TAG = Tag.Integer("key").defaultValue(0);

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        for (int i = 0; i < 1000; i++) {
            handler.updateAndGetTag(TAG, integer -> integer + 1);
        }
    }

    @Actor
    public void actor2() {
        for (int i = 0; i < 1000; i++) {
            handler.updateAndGetTag(TAG, integer -> integer + 1);
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = handler.getTag(TAG);
    }
}

