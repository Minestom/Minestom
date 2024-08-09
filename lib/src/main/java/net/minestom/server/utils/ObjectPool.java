package net.minestom.server.utils;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class ObjectPool<T> {
    private final Cleaner cleaner = Cleaner.create();
    private final MessagePassingQueue<SoftReference<T>> pool = new MpmcUnboundedXaddArrayQueue<>(256);
    private final Supplier<T> supplier;
    private final UnaryOperator<T> sanitizer;

    public ObjectPool(Supplier<T> supplier, UnaryOperator<T> sanitizer) {
        this.supplier = supplier;
        this.sanitizer = sanitizer;
    }

    public @NotNull T get() {
        T result;
        SoftReference<T> ref;
        while ((ref = pool.poll()) != null) {
            if ((result = ref.get()) != null) return result;
        }
        return supplier.get();
    }

    public @NotNull T getAndRegister(@NotNull Object ref) {
        T result = get();
        register(ref, result);
        return result;
    }

    public void add(@NotNull T object) {
        object = sanitizer.apply(object);
        this.pool.offer(new SoftReference<>(object));
    }

    public void clear() {
        this.pool.clear();
    }

    public int count() {
        return pool.size();
    }

    public void register(@NotNull Object ref, @NotNull AtomicReference<T> objectRef) {
        this.cleaner.register(ref, new BufferRefCleaner<>(this, objectRef));
    }

    public void register(@NotNull Object ref, @NotNull T object) {
        this.cleaner.register(ref, new BufferCleaner<>(this, object));
    }

    public void register(@NotNull Object ref, @NotNull Collection<T> objects) {
        this.cleaner.register(ref, new BuffersCleaner<>(this, objects));
    }

    public @NotNull Holder hold() {
        return new Holder(get());
    }

    public <R> R use(@NotNull Function<@NotNull T, R> function) {
        T object = get();
        try {
            return function.apply(object);
        } finally {
            add(object);
        }
    }

    private record BufferRefCleaner<T>(ObjectPool<T> pool, AtomicReference<T> objectRef) implements Runnable {
        @Override
        public void run() {
            this.pool.add(objectRef.get());
        }
    }

    private record BufferCleaner<T>(ObjectPool<T> pool, T object) implements Runnable {
        @Override
        public void run() {
            this.pool.add(object);
        }
    }

    private record BuffersCleaner<T>(ObjectPool<T> pool, Collection<T> objects) implements Runnable {
        @Override
        public void run() {
            for (T buffer : objects) {
                this.pool.add(buffer);
            }
        }
    }

    public final class Holder implements AutoCloseable {
        private final T object;
        private boolean closed;

        Holder(T object) {
            this.object = object;
        }

        public @NotNull T get() {
            if (closed) throw new IllegalStateException("Holder is closed");
            return object;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                add(object);
            }
        }
    }
}
