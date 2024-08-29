package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.Map;

import static net.kyori.adventure.nbt.IntBinaryTag.intBinaryTag;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "actor1", expect = ACCEPTABLE)
@Outcome(id = "actor2", expect = ACCEPTABLE)
@State
public class TagPathOverrideTest {
    private static final Tag<Integer> TAG_PATH = Tag.Integer("key").path("path");

    private final TagHandler handler = TagHandler.newHandler();

    @Actor
    public void actor1() {
        handler.setTag(TAG_PATH, 1);
    }

    @Actor
    public void actor2() {
        handler.setTag(TAG_PATH, 5);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        var compound = handler.asCompound();
        if (compound.equals(CompoundBinaryTag.from(Map.of("path", CompoundBinaryTag.from(Map.of("key", intBinaryTag(1))))))) {
            r.r1 = "actor1";
        } else if (compound.equals(CompoundBinaryTag.from(Map.of("path", CompoundBinaryTag.from(Map.of("key", intBinaryTag(5))))))) {
            r.r1 = "actor2";
        } else {
            r.r1 = compound;
        }
    }
}
