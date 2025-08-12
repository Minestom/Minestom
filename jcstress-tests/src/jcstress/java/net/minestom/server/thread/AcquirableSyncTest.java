package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "40101", expect = ACCEPTABLE)
@State
public class AcquirableSyncTest {

    private final ThreadDispatcher<World, Element> dispatcher = ThreadDispatcher.singleThread();
    private final World world = new World();
    private final Element element = new Element();

    record World() {
    }

    static final class Element implements Tickable, AcquirableSource<Element> {
        private final Acquirable<Element> acquirable = Acquirable.unassigned(this);
        int value;

        @Override
        public void tick(long time) {
            compute();
        }

        void compute() {
            value++;
        }

        @Override
        public @NotNull Acquirable<? extends Element> acquirable() {
            return acquirable;
        }
    }

    {
        dispatcher.createPartition(world);
        dispatcher.updateElement(element, world);
        dispatcher.start();
        dispatcher.refreshThreads();
        dispatcher.updateAndAwait(0);
    }

    private void loop() {
        for (int i = 0; i < 10_000; i++) {
            element.acquirable().sync(Element::compute);
        }
    }

    @Actor
    public void actor0() {
        for (int i = 0; i < 100; i++) {
            dispatcher.updateAndAwait(0);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Actor
    public void actor1() {
        TickThread tickThread = new TickThread(1) {
            @Override
            public void run() {
                loop();
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
                loop();
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
                loop();
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
    public void actor4() {
        Thread thread = new Thread(this::loop);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void arbiter(L_Result r) {
        element.acquirable().sync(test -> r.r1 = test.value);
        dispatcher.shutdown();
    }
}
