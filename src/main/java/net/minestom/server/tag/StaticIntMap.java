package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.function.Consumer;

sealed interface StaticIntMap<T> permits StaticIntMap.Array {

    T get(@Range(from = 0, to = Integer.MAX_VALUE) int key);

    void forValues(@NotNull Consumer<T> consumer);

    @NotNull StaticIntMap<T> copy();

    // Methods potentially causing re-hashing

    void put(@Range(from = 0, to = Integer.MAX_VALUE) int key, T value);

    void remove(@Range(from = 0, to = Integer.MAX_VALUE) int key);

    void updateContent(@NotNull StaticIntMap<T> content);

    final class Array<T> implements StaticIntMap<T> {
        private static final Object[] EMPTY_ARRAY = new Object[0];

        private T[] array;

        public Array(T[] array) {
            this.array = array;
        }

        public Array() {
            //noinspection unchecked
            this.array = (T[]) EMPTY_ARRAY;
        }

        @Override
        public T get(int key) {
            final T[] array = this.array;
            return key < array.length ? array[key] : null;
        }

        @Override
        public void forValues(@NotNull Consumer<T> consumer) {
            final T[] array = this.array;
            for (T value : array) {
                if (value != null) consumer.accept(value);
            }
        }

        @Override
        public @NotNull StaticIntMap<T> copy() {
            return new Array<>(array.clone());
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
        public void updateContent(@NotNull StaticIntMap<T> content) {
            if (content instanceof StaticIntMap.Array<T> arrayMap) {
                updateArray(arrayMap.array.clone());
            } else {
                throw new IllegalArgumentException("Invalid content type: " + content.getClass());
            }
        }

        @Override
        public void remove(int key) {
            T[] array = this.array;
            if (key < array.length) array[key] = null;
        }

        T[] updateArray(T[] result) {
            this.array = result;
            return result;
        }
    }
}
