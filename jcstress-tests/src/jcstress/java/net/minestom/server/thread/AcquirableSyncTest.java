package net.minestom.server.thread;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "30000", expect = ACCEPTABLE)
@State
public class AcquirableSyncTest {

    private final TickThread mainThread = new TickThread(0);
    private final Acquirable<Test> acquirable = Acquirable.of(new Test());

    {
        ((AcquirableImpl<Test>) acquirable).updateThread(mainThread);
    }

    static final class Test {
        int value;
    }

    @Actor
    public void actor1() {
        TickThread tickThread = new TickThread(1) {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < 10_000; i++) {
                    acquirable.sync(test -> {
                        test.value = test.value + 1;
                    });
                }
            }
        };
        tickThread.start();
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        TickThread tickThread = new TickThread(2) {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < 10_000; i++) {
                    acquirable.sync(test -> {
                        test.value = test.value + 1;
                    });
                }
            }
        };
        tickThread.start();
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor3() {
        TickThread tickThread = new TickThread(3) {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < 10_000; i++) {
                    acquirable.sync(test -> {
                        test.value = test.value + 1;
                    });
                }
            }
        };
        tickThread.start();
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        acquirable.sync(test -> r.r1 = test.value);
    }
}
