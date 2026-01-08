package net.minestom.server.instance.light;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@ApiStatus.Experimental
public interface LightEngine {
    ExecutorService workerService();

    CompletableFuture<@Nullable Void> scheduleFutureWork(BooleanSupplier precondition, Runnable work);

    <WorkKey> CompletableFuture<@Nullable Void> scheduleFutureWork(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, BooleanSupplier precondition, Runnable work);

    static LightEngine getDefault() {
        return DefaultLightEngine.instance();
    }

    record WorkEntry(BooleanSupplier precondition, Runnable work, CompletableFuture<@Nullable Void> future) {
    }

    sealed interface WorkTypeTracker<WorkKey> {
        @Nullable WorkEntry poll();

        CompletableFuture<@Nullable Void> add(WorkKey workKey, BooleanSupplier precondition, Runnable work);

        AtomicInteger pollers();

        boolean hasWork();

        final class Hash<WorkKey> implements WorkTypeTracker<WorkKey> {
            private static final Exception CANCELLED = new Exception("Cancelled");
            private final AtomicInteger pollers = new AtomicInteger();
            private final ConcurrentHashMap<WorkKey, WorkEntry> work = new ConcurrentHashMap<>();
            private final AtomicInteger size = new AtomicInteger();

            @Override
            public @Nullable WorkEntry poll() {
                while (true) {
                    if (work.isEmpty()) return null;
                    for (var entry : work.entrySet()) {
                        if (work.remove(entry.getKey(), entry.getValue())) {
                            // Successful remove.
                            size.decrementAndGet();
                            return entry.getValue();
                        }
                    }
                }
            }

            @Override
            public boolean hasWork() {
                return size.get() > 0;
            }

            @Override
            public CompletableFuture<@Nullable Void> add(WorkKey workKey, BooleanSupplier precondition, Runnable work) {
                var future = new CompletableFuture<@Nullable Void>();
                var old = this.work.put(workKey, new WorkEntry(precondition, work, future));
                if (old != null) {
                    old.future().completeExceptionally(CANCELLED);
                } else {
                    size.incrementAndGet();
                }
                return future;
            }

            @Override
            public AtomicInteger pollers() {
                return pollers;
            }
        }
    }
}
