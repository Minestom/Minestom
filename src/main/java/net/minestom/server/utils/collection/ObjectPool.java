package net.minestom.server.utils.collection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
@ApiStatus.Experimental
public sealed interface ObjectPool<T> permits ObjectPoolImpl, ObjectPoolImpl.Unpooled {
    static <T> ObjectPool<T> pool(int size, Supplier<T> supplier, UnaryOperator<T> sanitizer) {
        if (size <= 0) return new ObjectPoolImpl.Unpooled<>(supplier);
        return new ObjectPoolImpl<>(size, supplier, sanitizer);
    }

    static <T> ObjectPool<T> pool(int size, Supplier<T> supplier) {
        if (size <= 0) return new ObjectPoolImpl.Unpooled<>(supplier);
        return new ObjectPoolImpl<>(size, supplier, UnaryOperator.identity());
    }

    T get();

    T getAndRegister(Object ref);

    void add(T object);

    void clear();

    int count();

    void register(Object ref, AtomicReference<T> objectRef);

    void register(Object ref, T object);

    void register(Object ref, Collection<T> objects);

    Holder<T> hold();

    <R> R use(Function<? super T, R> function);

    interface Holder<T> extends Supplier<T>, AutoCloseable {
        @Override
        void close();
    }
}
