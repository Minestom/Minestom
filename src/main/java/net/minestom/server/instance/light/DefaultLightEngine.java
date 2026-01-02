package net.minestom.server.instance.light;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public final class DefaultLightEngine implements LightEngine {
    private static final Exception PRECONDITION_FAILED = new Exception("Precondition failed");
    private static final DefaultLightEngine INSTANCE = new DefaultLightEngine();
    // We can immediately submit just over the available processor count tasks. All threads should be busy the entire time.
    private final Semaphore freeSubmits = new Semaphore(Runtime.getRuntime().availableProcessors() + 2);
    @ApiStatus.Internal
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
        MinecraftServer.getSchedulerManager().buildShutdownTask(this.workerService::shutdown);
    }

    @Override
    public ExecutorService workerService() {
        return workerService;
    }

    private void scheduleThenRelease(CompletableFuture<@Nullable Void> future, BooleanSupplier precondition, Runnable work) {
        workerService.submit(() -> {
            try {
                if (!precondition.getAsBoolean()) {
                    future.completeExceptionally(PRECONDITION_FAILED);
                    return;
                }
                work.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            } finally {
                freeSubmits.release();
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable Void> scheduleFutureWork(BooleanSupplier precondition, Runnable work) {
        var future = new CompletableFuture<@Nullable Void>();
//        WORKING_COUNT.incrementAndGet();
//        future.whenComplete((_, _) -> WORKING_COUNT.decrementAndGet());
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
    public <WorkKey> CompletableFuture<@Nullable Void> scheduleFutureWork(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, BooleanSupplier precondition, Runnable
            work) {
        var future = tracker.add(workKey, precondition, work);
//        WORKING_COUNT.incrementAndGet();
//        future.whenComplete((_, _) -> WORKING_COUNT.decrementAndGet());
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
                MinecraftServer.getExceptionManager().handleException(t);
            }
        });
    }

    private <T> void handleEntry(WorkEntry entry) {
        scheduleThenRelease(entry.future(), entry.precondition(), entry.work());
    }

    public static LightEngine instance() {
        return INSTANCE;
    }
}
