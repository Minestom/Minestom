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
    private static final int THREAD_COUNT = 10;

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
        this.mainThread = new TickThread(0) {
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
        this.acquirable = Acquirable.of(new Test());
        ((AcquirableImpl<Test>) acquirable).updateThread(mainThread);

        {
            this.tickThreads = new ArrayList<>(THREAD_COUNT);
            for (int i = 0; i < THREAD_COUNT; i++) {
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
            this.threads = new ArrayList<>(THREAD_COUNT);
            for (int i = 0; i < THREAD_COUNT; i++) {
                Thread thread = new Thread(() -> consumer.accept(acquirable));
                threads.add(thread);
            }
        }
    }

    @Benchmark
    public void localUnsafe() {
        launchLocal((acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.unwrap().value++;
        });
    }

    @Benchmark
    public void localSync() {
        launchLocal((acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.sync(test -> test.value++);
        });
    }

    @Benchmark
    public void localSynchronizedKeyword() {
        Object object = new Object();
        launchLocal((acquirable) -> {
            for (int i = 0; i < 10_000; i++) {
                synchronized (object) {
                    acquirable.unwrap().value++;
                }
            }
        });
    }

    @Benchmark
    public void foreignSync() {
        // Single thread (not main) acquiring the element
        launch(tickThreads.subList(0, 1), (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.sync(test -> test.value++);
        });
    }

    @Benchmark
    public void unsafe() {
        launch(threads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.unwrap().value++;
        });
    }

    @Benchmark
    public void synchronizedKeyword() {
        Object object = new Object();
        launch(threads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) {
                synchronized (object) {
                    acquirable.unwrap().value++;
                }
            }
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

    @Benchmark
    public void multiDoubleAcquireTickThread() {
        launch(tickThreads, (acquirable) -> {
            for (int i = 0; i < 10_000; i++) acquirable.sync(t -> acquirable.sync(test -> test.value++));
        });
    }

    private void launch(List<Thread> threads, Consumer<Acquirable<Test>> consumer) {
        final int factor = THREAD_COUNT / threads.size();
        this.consumer = acquirable -> {
            for (int i = 0; i < factor; i++) {
                consumer.accept(acquirable);
            }
        };
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

    private void launchLocal(Consumer<Acquirable<Test>> consumer) {
        // Multiply by thread count to simulate the same amount of operations
        this.consumer = acquirable -> {
            for (int i = 0; i < THREAD_COUNT; i++) {
                consumer.accept(acquirable);
            }
        };
        this.mainThread.start();
        try {
            this.mainThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
