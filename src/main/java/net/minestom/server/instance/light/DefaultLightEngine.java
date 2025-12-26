package net.minestom.server.instance.light;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public final class DefaultLightEngine implements LightEngine {
    private static final Exception PRECONDITION_FAILED = new Exception("Precondition failed");
    private static final DefaultLightEngine INSTANCE = new DefaultLightEngine();
    // We can immediately submit just over the available processor count tasks. All threads should be busy the entire time.
    private final Semaphore freeSubmits = new Semaphore(Runtime.getRuntime().availableProcessors() + 2);
    public static final AtomicInteger WORKING_COUNT = new AtomicInteger();
    private final ExecutorService workerService;

    private DefaultLightEngine() {
        var factory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            private final AtomicInteger id = new AtomicInteger(1);

            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                var thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("LightEngine-" + id.getAndIncrement());
                return thread;
            }
        };
        this.workerService = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), factory, null, false);
    }

    @Override
    public ExecutorService workerService() {
        return workerService;
    }

    private <T> void scheduleThenRelease(CompletableFuture<T> future, BooleanSupplier precondition, Callable<T> work) {
        workerService.submit(() -> {
            try {
                if (!precondition.getAsBoolean()) {
                    future.completeExceptionally(PRECONDITION_FAILED);
                    return;
                }
                future.complete(work.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            } finally {
                freeSubmits.release();
            }
        });
    }

    @Override
    public <T> CompletableFuture<T> scheduleFutureWork(BooleanSupplier precondition, Callable<T> work) {
        var future = new CompletableFuture<T>();
        WORKING_COUNT.incrementAndGet();
        future.whenComplete((_, _) -> WORKING_COUNT.decrementAndGet());
//        future.exceptionally(_ -> null).orTimeout(15 ,TimeUnit.SECONDS).exceptionally(t -> {
//            t.printStackTrace();
//            return null;
//        });
        if (freeSubmits.tryAcquire()) {
            // Short-circuit virtual thread
            scheduleThenRelease(future, precondition, work);
        } else {
            Thread.startVirtualThread(() -> {
                try {
                    freeSubmits.acquire();
                    if (!precondition.getAsBoolean()) {
                        future.completeExceptionally(PRECONDITION_FAILED);
                        freeSubmits.release();
                        return;
                    }
                    scheduleThenRelease(future, precondition, work);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                    freeSubmits.release();
                }
            });
        }
        return future;
    }

    @Override
    public <WorkKey, T> CompletableFuture<T> scheduleFutureWork(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, BooleanSupplier precondition, Callable<T> work) {
//        if (true) return scheduleFutureWork(precondition, work);
        var future = tracker.add(workKey, precondition, work);
//        var original = new Exception();
//        future.exceptionally(t -> null).orTimeout(5, TimeUnit.SECONDS).exceptionally(t -> {
//            t.addSuppressed(original);
//            t.printStackTrace();
//            return null;
//        });
        WORKING_COUNT.incrementAndGet();
        future.whenComplete((_, _) -> WORKING_COUNT.decrementAndGet());
//        future.exceptionally(_ -> null).orTimeout(5 ,TimeUnit.SECONDS).exceptionally(t -> {
//            t.printStackTrace();
//            return null;
//        });
        var pollers = tracker.pollers();
        if (pollers.get() > 0) {
            // At least 1 poller after we have submitted. The task will be found.
            return future;
        }
        startPoller(tracker);
        return future;
    }

    private <WorkType> void startPoller(WorkTypeTracker<WorkType> tracker) {
        var pollers = tracker.pollers();
        pollers.incrementAndGet();
        // We have to start a poller
        Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    while (true) {
                        var entry = tracker.poll();
                        if (entry == null) break; // No more work.
                        try {
                            freeSubmits.acquire();
                        } catch (InterruptedException t) {
                            entry.future().completeExceptionally(t);
                            continue;
                        }
                        if (!entry.precondition().getAsBoolean()) {
                            entry.future().completeExceptionally(PRECONDITION_FAILED);
                            freeSubmits.release();
                            continue;
                        }
                        handleEntry(entry);
                    }
                    // No more immediate work. Try to stop
                    pollers.decrementAndGet();
                    if (tracker.hasWork()) {
                        // New work came in while trying to stop
                        pollers.incrementAndGet();
                        continue;
                    }
                    // No new work, we can exit
                    break;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private <T> void handleEntry(WorkEntry<T> entry) {
        scheduleThenRelease(entry.future(), entry.precondition(), entry.work());
    }

    public static LightEngine instance() {
        return INSTANCE;
    }
}
