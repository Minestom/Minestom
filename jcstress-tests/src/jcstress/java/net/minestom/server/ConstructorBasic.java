package net.minestom.server;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

@JCStressTest
@Outcome(id = "-1", expect = ACCEPTABLE, desc = "Object has not been seen")
@Outcome(id = {"4", "5"}, expect = ACCEPTABLE_INTERESTING, desc = "Object only partially initialized")
@Outcome(id = "9", expect = ACCEPTABLE,  desc = "Object fully initialized")
@State
public class ConstructorBasic {
    private SimpleClass value;

    @Actor
    public void actor1() {
        value = new SimpleClass();
    }

    @Actor
    public void actor2(I_Result r) {
        SimpleClass current = this.value;
        if (current != null) {
            r.r1 = current.getX() + current.getY();
        } else {
            r.r1 = -1;
        }
    }

    public static class SimpleClass {
        private int x;
        private int y;

        SimpleClass() {
            x = 4;
            y = 5;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }
    }
}