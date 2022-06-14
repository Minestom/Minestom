package net.minestom.server.utils.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;

final class ObjectArrayImpl {
    static final class SingleThread<T> implements ObjectArray<T> {
        private T[] array;
        private int max;

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
            T[] array = this.array;
            if (index >= array.length) {
                final int newLength = index * 2 + 1;
                this.array = array = Arrays.copyOf(array, newLength);
            }
            array[index] = object;
            this.max = Math.max(max, index);
        }

        @Override
        public void trim() {
            this.array = Arrays.copyOf(array, max + 1);
        }

        @Override
        public @UnknownNullability T @NotNull [] arrayCopy(@NotNull Class<T> type) {
            //noinspection unchecked,rawtypes
            return (T[]) Arrays.<T, T>copyOf(array, max + 1, (Class) type.arrayType());
        }
    }

    static final class Concurrent<T> implements ObjectArray<T> {
        private volatile T[] array;
        private int max;

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
            T[] array = this.array;
            if (index >= array.length) {
                final int newLength = index * 2 + 1;
                this.array = array = Arrays.copyOf(array, newLength);
            }
            array[index] = object;
            this.max = Math.max(max, index);
        }

        @Override
        public synchronized void trim() {
            this.array = Arrays.copyOf(array, max + 1);
        }

        @Override
        public @UnknownNullability T @NotNull [] arrayCopy(@NotNull Class<T> type) {
            //noinspection unchecked,rawtypes
            return (T[]) Arrays.<T, T>copyOf(array, max + 1, (Class) type.arrayType());
        }
    }
}
