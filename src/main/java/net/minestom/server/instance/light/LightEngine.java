package net.minestom.server.instance.light;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

@ApiStatus.Experimental
public interface LightEngine {
    ExecutorService workerService();

    <T> CompletableFuture<@UnknownNullability T> scheduleFutureWork(BooleanSupplier precondition, Callable<@UnknownNullability T> work);

    <WorkKey, T> CompletableFuture<@UnknownNullability T> scheduleFutureWork(WorkTypeTracker<WorkKey> tracker, WorkKey workKey, BooleanSupplier precondition, Callable<@UnknownNullability T> work);

    static LightEngine getDefault() {
        return DefaultLightEngine.instance();
    }

    record WorkEntry<T>(BooleanSupplier precondition, Callable<T> work, CompletableFuture<T> future,
                        AtomicBoolean submitted) {
    }

    sealed interface WorkTypeTracker<WorkKey> {
        @Nullable WorkEntry<?> poll();

        <T> CompletableFuture<@UnknownNullability T> add(WorkKey workKey, BooleanSupplier precondition, Callable<@UnknownNullability T> work);

        AtomicInteger pollers();

        boolean hasWork();

        final class Hash<WorkKey> implements WorkTypeTracker<WorkKey> {
            private static final Exception CANCELLED = new Exception("Cancelled");
            private final AtomicInteger pollers = new AtomicInteger();
            private final ConcurrentHashMap<WorkKey, WorkEntry<?>> work = new ConcurrentHashMap<>();
            private final AtomicInteger size = new AtomicInteger();

            @Override
            public @Nullable WorkEntry<?> poll() {
                var it = work.values().iterator();
                if (!it.hasNext()) return null;
                var element = it.next();
                it.remove();
                size.decrementAndGet();
                return element;
            }

            @Override
            public boolean hasWork() {
                return size.get() > 0;
            }

            @Override
            public <T> CompletableFuture<T> add(WorkKey workKey, BooleanSupplier precondition, Callable<T> work) {
                var future = new CompletableFuture<T>();
                var old = this.work.put(workKey, new WorkEntry<>(precondition, work, future, new AtomicBoolean()));
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
