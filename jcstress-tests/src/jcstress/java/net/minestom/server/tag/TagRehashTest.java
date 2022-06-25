package net.minestom.server.tag;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.LL_Result;

import java.util.ArrayList;
import java.util.List;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "1, 198", expect = ACCEPTABLE)
@Outcome(id = "1, 99", expect = ACCEPTABLE)
@Outcome(id = "2, 198", expect = ACCEPTABLE)
@Outcome(id = "2, 99", expect = ACCEPTABLE)
@State
public class TagRehashTest {
    private static final int MAX_SIZE = 500;
    private static final List<Tag<Integer>> TAGS;

    static {
        List<Tag<Integer>> tags = new ArrayList<>();
        for (int i = 0; i < MAX_SIZE; i++) {
            tags.add(Tag.Integer("key" + i));
        }
        TAGS = List.copyOf(tags);
    }

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        for (int i = 0; i < MAX_SIZE; i++) {
            handler.setTag(TAGS.get(i), i);
        }
    }

    @Actor
    public void actor2() {
        for (int i = 0; i < MAX_SIZE; i++) {
            handler.setTag(TAGS.get(i), i * 2);
        }
    }

    @Arbiter
    public void arbiter(LL_Result r) {
        r.r1 = handler.getTag(TAGS.get(1));
        r.r2 = handler.getTag(TAGS.get(99));
    }
}
