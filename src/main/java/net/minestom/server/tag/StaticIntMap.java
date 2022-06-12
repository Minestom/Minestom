package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

sealed interface StaticIntMap<T> permits StaticIntMap.Array {

    T get(@Range(from = 0, to = Integer.MAX_VALUE) int key);

    void put(@Range(from = 0, to = Integer.MAX_VALUE) int key, T value);

    boolean compareAndSet(@Range(from = 0, to = Integer.MAX_VALUE) int key, T expected, T updated);

    void remove(@Range(from = 0, to = Integer.MAX_VALUE) int key);

    T getAndRemove(@Range(from = 0, to = Integer.MAX_VALUE) int key);

    void forValues(@NotNull Consumer<T> consumer);

    boolean isEmpty();

    void updateContent(@NotNull StaticIntMap<T> content);

    @NotNull StaticIntMap<T> copy();

    final class Array<T> implements StaticIntMap<T> {
        private static final Object[] EMPTY_ARRAY = new Object[0];
        private static final VarHandle ARRAY_UPDATER = MethodHandles.arrayElementVarHandle(Object[].class);

        private final AtomicInteger counter;
        private T[] array;

        public Array(AtomicInteger counter, T[] array) {
            this.counter = counter;
            this.array = array;
        }

        public Array(AtomicInteger counter) {
            this.counter = counter;
            //noinspection unchecked
            this.array = (T[]) EMPTY_ARRAY;
        }

        @Override
        public T get(int key) {
            final T[] array = this.array;
            return key < array.length ? array[key] : null;
        }

        @Override
        public void put(int key, T value) {
            T[] array = this.array;
            if (key >= array.length) {
                array = updateArray(Arrays.copyOf(array, key * 2 + 1));
            }
            array[key] = value;
        }

        @Override
        public boolean compareAndSet(@Range(from = 0, to = Integer.MAX_VALUE) int key, T expected, T updated) {
            T[] array = this.array;
            if (key >= array.length) {
                updateArray(Arrays.copyOf(array, key * 2 + 1));
                return false;
            }
            return ARRAY_UPDATER.compareAndSet(array, key, expected, updated);
        }

        @Override
        public void remove(int key) {
            T[] array = this.array;
            if (key < array.length) array[key] = null;
        }

        @Override
        public T getAndRemove(int key) {
            T[] array = this.array;
            if (key >= 0 && key < array.length) {
                final T value = array[key];
                array[key] = null;
                return value;
            }
            return null;
        }

        @Override
        public void forValues(@NotNull Consumer<T> consumer) {
            final T[] array = this.array;
            for (T value : array) {
                if (value != null) consumer.accept(value);
            }
        }

        @Override
        public boolean isEmpty() {
            for (T value : this.array) {
                if (value != null) return false;
            }
            return true;
        }

        @Override
        public void updateContent(@NotNull StaticIntMap<T> content) {
            if (content instanceof StaticIntMap.Array<T> arrayMap) {
                updateArray(arrayMap.array.clone());
            } else {
                throw new IllegalArgumentException("Invalid content type: " + content.getClass());
            }
        }

        @Override
        public @NotNull StaticIntMap<T> copy() {
            return new Array<>(counter, array.clone());
        }

        T[] updateArray(T[] result) {
            this.counter.incrementAndGet();
            this.array = result;
            return result;
        }
    }
}
