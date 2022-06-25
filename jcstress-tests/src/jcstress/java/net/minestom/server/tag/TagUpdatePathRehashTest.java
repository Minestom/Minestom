package net.minestom.server.tag;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.ArrayList;
import java.util.List;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "2000", expect = ACCEPTABLE)
@State
public class TagUpdatePathRehashTest {
    private static final Tag<Integer> TAG = Tag.Integer("key").path("path").defaultValue(0);

    private static final int MAX_SIZE = 500;
    private static final List<Tag<Integer>> TAGS;

    static {
        List<Tag<Integer>> tags = new ArrayList<>();
        for (int i = 0; i < MAX_SIZE; i++) {
            tags.add(Tag.Integer("key" + i).path("path"));
        }
        TAGS = List.copyOf(tags);
    }

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        for (int i = 0; i < 1000; i++) {
            handler.updateTag(TAG, integer -> integer + 1);
        }
    }

    @Actor
    public void actor2() {
        for (int i = 0; i < 1000; i++) {
            handler.updateTag(TAG, integer -> integer + 1);
        }
    }

    @Actor
    public void actor3() {
        // May be able to disturb actor1/2
        for (int i = 0; i < MAX_SIZE; i++) {
            handler.setTag(TAGS.get(i), i);
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = handler.getTag(TAG);
    }
}

