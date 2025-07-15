package net.minestom.server.utils.collection;

import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.List;

final class ObjectArrayImpl {
    static final class SingleThread<T> implements ObjectArray<T> {
        private T[] array;
        private int max = -1;

        SingleThread(int size) {
            //noinspection unchecked
            this.array = (T[]) new Object[size];
        }

        @Override
        public @UnknownNullability T get(int index) {
            final T[] array = this.array;
            return index < array.length ? array[index] : null;
        }

        @Override
        public void set(int index, @Nullable T object) {
            if (object == null) {
                remove(index);
                return;
            }
            T[] array = this.array;
            if (index >= array.length) {
                final int newLength = index * 2 + 1;
                this.array = array = Arrays.copyOf(array, newLength);
            }
            array[index] = object;
            this.max = Math.max(max, index);
        }

        @Override
        public void remove(int index) {
            final T[] array = this.array;
            if (index >= array.length) return; // Will be null anyway
            array[index] = null;
            // Now we need to backtrack the max index,
            // For example [0, 1, 2, null, 4] removing 4 requires us to backtrack past the null
            final int max = this.max;
            if (max == index) {
                int lastNotNull = max - 1;
                while (lastNotNull >= 0 && array[lastNotNull] == null) {
                    lastNotNull--;
                }
                this.max = lastNotNull;
            }
        }

        @Override
        public void trim() {
            this.array = Arrays.copyOf(array, max + 1);
        }

        @Override
        public @UnknownNullability T [] arrayCopy(Class<T> type) {
            //noinspection unchecked,rawtypes
            return (T[]) Arrays.<T, T>copyOf(array, max + 1, (Class) type.arrayType());
        }

        @Override
        public List<T> toList() {
            // Trim the array to the maximum size, it internally will be copied regardless.
            final T[] array = Arrays.copyOf(this.array, max + 1);
            return List.of(array);
        }
    }

    static final class Concurrent<T> implements ObjectArray<T> {
        private volatile T[] array;
        private volatile int max = -1;

        Concurrent(int size) {
            //noinspection unchecked
            this.array = (T[]) new Object[size];
        }

        @Override
        public @UnknownNullability T get(int index) {
            final T[] array = this.array;
            return index < array.length ? array[index] : null;
        }

        @Override
        public synchronized void set(int index, @Nullable T object) {
            if (object == null) {
                remove(index);
                return;
            }
            T[] array = this.array;
            if (index >= array.length) {
                final int newLength = index * 2 + 1;
                this.array = array = Arrays.copyOf(array, newLength);
            }
            array[index] = object;
            this.max = Math.max(max, index);
        }

        @Override
        public synchronized void remove(int index) {
            final T[] array = this.array;
            if (index >= array.length) return; // Will be null anyway
            array[index] = null;
            // Now we need to backtrack the max index,
            // For example [0, 1, 2, null, 4] removing 4 requires us to backtrack past the null
            final int max = this.max;
            if (max == index) {
                int lastNotNull = max - 1;
                while (lastNotNull >= 0 && array[lastNotNull] == null) {
                    lastNotNull--;
                }
                this.max = lastNotNull;
            }
        }

        @Override
        public synchronized void trim() {
            this.array = Arrays.copyOf(array, max + 1);
        }

        @Override
        public @UnknownNullability T [] arrayCopy(Class<T> type) {
            //noinspection unchecked,rawtypes
            return (T[]) Arrays.<T, T>copyOf(array, max + 1, (Class) type.arrayType());
        }

        @Override
        public List<T> toList() {
            // Trim the array to the maximum size, it internally will be copied regardless.
            final T[] array = Arrays.copyOf(this.array, this.max + 1);
            return List.of(array);
        }
    }
}
