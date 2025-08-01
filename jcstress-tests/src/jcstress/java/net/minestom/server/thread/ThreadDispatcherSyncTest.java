package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

@JCStressTest
@Outcome(id = "301", expect = ACCEPTABLE)
@State
public class ThreadDispatcherSyncTest {
    private final ThreadDispatcher<World> dispatcher = ThreadDispatcher.singleThread();
    private final World world = new World();
    private final Element element = new Element();

    record World() {
    }

    static final class Element implements Tickable {
        int value;

        @Override
        public void tick(long time) {
            compute();
        }

        void compute() {
            value++;
        }
    }

    {
        dispatcher.createPartition(world);
        dispatcher.updateElement(element, world);
        dispatcher.start();
        dispatcher.refreshThreads();
        dispatcher.updateAndAwait(0);
    }

    @Actor
    public void actor1() {
        for (int i = 0; i < 100; i++) dispatcher.updateAndAwait(0);
    }

    @Actor
    public void actor2() {
        for (int i = 0; i < 100; i++) dispatcher.updateAndAwait(0);
    }

    @Actor
    public void actor3() {
        for (int i = 0; i < 100; i++) dispatcher.updateAndAwait(0);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        r.r1 = element.value;
        dispatcher.shutdown();
    }
}
