package net.minestom.server.utils;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class ObjectPool<T> {
    private static final int QUEUE_SIZE = 32_768;
    private static final Cleaner CLEANER = Cleaner.create();

    private final MessagePassingQueue<SoftReference<T>> pool = new MpmcUnboundedXaddArrayQueue<>(QUEUE_SIZE);
    private final Supplier<T> supplier;
    private final UnaryOperator<T> sanitizer;

    public static <T> ObjectPool<T> pool(Supplier<T> supplier, UnaryOperator<T> sanitizer) {
        return new ObjectPool<>(supplier, sanitizer);
    }

    public static <T> ObjectPool<T> pool(Supplier<T> supplier) {
        return new ObjectPool<>(supplier, UnaryOperator.identity());
    }

    private ObjectPool(Supplier<T> supplier, UnaryOperator<T> sanitizer) {
        this.supplier = supplier;
        this.sanitizer = sanitizer;
    }

    public T get() {
        T result;
        SoftReference<T> ref;
        while ((ref = pool.poll()) != null) {
            if ((result = ref.get()) != null) return result;
        }
        return supplier.get();
    }

    public T getAndRegister(Object ref) {
        T result = get();
        register(ref, result);
        return result;
    }

    public void add(T object) {
        object = sanitizer.apply(object);
        this.pool.offer(new SoftReference<>(object));
    }

    public void clear() {
        this.pool.clear();
    }

    public int count() {
        return pool.size();
    }

    public void register(Object ref, AtomicReference<T> objectRef) {
        CLEANER.register(ref, new BufferRefCleaner<>(this, objectRef));
    }

    public void register(Object ref, T object) {
        CLEANER.register(ref, new BufferCleaner<>(this, object));
    }

    public void register(Object ref, Collection<T> objects) {
        CLEANER.register(ref, new BuffersCleaner<>(this, objects));
    }

    public Holder hold() {
        return new Holder(get());
    }

    public <R> R use(Function<T, R> function) {
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
        private final AtomicBoolean closed = new AtomicBoolean(false);

        Holder(T object) {
            this.object = object;
        }

        public T get() {
            if (closed.get()) throw new IllegalStateException("Holder is closed");
            return object;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                add(object);
            }
        }
    }
}
