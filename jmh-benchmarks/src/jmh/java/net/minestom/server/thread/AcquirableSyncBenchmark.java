package net.minestom.server.thread;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AcquirableSyncBenchmark {

    TickThread mainThread;
    Acquirable<Test> acquirable;

    List<Thread> tickThreads;
    List<Thread> threads;

    Consumer<Acquirable<Test>> consumer;

    static final class Test {
        int value;
    }

    @Setup(Level.Invocation)
    public void setup() {
        mainThread = new TickThread(0);
        acquirable = Acquirable.of(new Test());
        ((AcquirableImpl<Test>) acquirable).updateThread(mainThread);

        {
            this.tickThreads = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TickThread thread = new TickThread(i + 1) {
                    @Override
                    public void run() {
                        this.lock().lock();
                        try {
                            consumer.accept(acquirable);
                        } finally {
                            this.lock().unlock();
                        }
                    }
                };
                tickThreads.add(thread);
            }
        }

        {
            this.threads = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Thread thread = new Thread(() -> consumer.accept(acquirable));
                threads.add(thread);
            }
        }
    }

    @Benchmark
    public void unsafe() {
        launch(threads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.unwrap().value++;
        });
    }

    @Benchmark
    public void multiAcquireThread() {
        launch(threads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.sync(test -> test.value++);
        });
    }

    @Benchmark
    public void multiAcquireTickThread() {
        launch(tickThreads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.sync(test -> test.value++);
        });
    }

    private void launch(List<Thread> threads, Consumer<Acquirable<Test>> consumer) {
        this.consumer = consumer;
        // Start all
        for (Thread thread : threads) thread.start();
        // Wait for all to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
