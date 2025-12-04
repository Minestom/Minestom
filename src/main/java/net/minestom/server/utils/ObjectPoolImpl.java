package net.minestom.server.utils;

import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// This is important to be a record cause of static final field trusting stops
// once the terminator in the chain is no longer foldable, so regular final field previously
record ObjectPoolImpl<T>(Queue<SoftReference<T>> pool, Supplier<T> supplier,
                         UnaryOperator<T> sanitizer) implements ObjectPool<T> {
    private static final Cleaner CLEANER = Cleaner.create();

    public ObjectPoolImpl(int size, Supplier<T> supplier, UnaryOperator<T> sanitizer) {
        this(new MpmcUnboundedXaddArrayQueue<>(size), supplier, sanitizer);
    }

    @Override
    public T get() {
        T result;
        SoftReference<T> ref;
        while ((ref = this.pool.poll()) != null) {
            if ((result = ref.get()) != null) return result;
        }
        return this.supplier.get();
    }

    @Override
    public T getAndRegister(Object ref) {
        T result = get();
        register(ref, result);
        return result;
    }

    @Override
    public void add(T object) {
        object = this.sanitizer.apply(object);
        this.pool.offer(new SoftReference<>(object));
    }

    @Override
    public void clear() {
        this.pool.clear();
    }

    @Override
    public int count() {
        return this.pool.size();
    }

    @Override
    public void register(Object ref, AtomicReference<T> objectRef) {
        CLEANER.register(ref, new BufferRefCleaner<>(this, objectRef));
    }

    @Override
    public void register(Object ref, T object) {
        CLEANER.register(ref, new BufferCleaner<>(this, object));
    }

    @Override
    public void register(Object ref, Collection<T> objects) {
        CLEANER.register(ref, new BuffersCleaner<>(this, objects));
    }

    @Override
    public Holder<T> hold() {
        return new HolderImpl<>(this, get());
    }

    @Override
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

    private record HolderImpl<T>(ObjectPool<T> pool, T object, AtomicBoolean closed) implements Holder<T> {
        HolderImpl(ObjectPool<T> pool, T object) {
            this(pool, object, new AtomicBoolean(false));
        }

        @Override
        public T get() {
            if (closed.get()) throw new IllegalStateException("Holder is closed");
            return object;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                pool.add(object);
            }
        }
    }

    // Simple pool implementation
    record Unpooled<T>(Supplier<T> supplier) implements ObjectPool<T> {
        @Override
        public T get() {
            return supplier.get();
        }

        @Override
        public T getAndRegister(Object ref) {
            return supplier.get();
        }

        @Override
        public void add(T object) {
            // noop
        }

        @Override
        public void clear() {
            // noop
        }

        @Override
        public int count() {
            return 0;
        }

        @Override
        public void register(Object ref, AtomicReference<T> objectRef) {
            // noop
        }

        @Override
        public void register(Object ref, T object) {
            // noop
        }

        @Override
        public void register(Object ref, Collection<T> objects) {
            // noop
        }

        @Override
        public Holder<T> hold() {
            return new HolderImpl<>(this, get());
        }

        @Override
        public <R> R use(Function<T, R> function) {
            return function.apply(get());
        }
    }
}
