package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A list that allows reusing its iterators, and other aspects, to minimize allocations.
 */
class ReusableList<T> implements Iterable<T> {
    private @Nullable ReusableIterator iterator = new ReusableIterator();
    private @Nullable ReusableIterator lastIterator;
    private Object[] data = new Object[16];
    private int modCount;
    private int size;

    public void add(T value) {
        ensureCapacity(size + 1);
        data[size++] = value;
        modCount++;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        // TODO do we want to clear the array?
        //  Keeping data may hurt if it lands in old gen,
        //  gotta do some benchmarks to see which is better
        size = 0;
        modCount++;
    }

    private void ensureCapacity(int minCapacity) {
        if (data.length >= minCapacity) return;

        var newLength = data.length;
        while (newLength <= minCapacity) newLength <<= 1;

        var newData = new Object[newLength];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
    }

    @SuppressWarnings("unchecked")
    List<T> collect() {
        if (size == 0) return List.of();
        return (List<T>) List.of(Arrays.copyOf(data, size));
    }

    @Override
    public Iterator<T> iterator() {
        if (iterator != null) return lastIterator = iterator.reset();
        return lastIterator = new ReusableIterator().reset();
    }

    public void reuseLastIterator() {
        if (lastIterator != null) {
            lastIterator.reuse();
            lastIterator = null;
        }
    }

    public class ReusableIterator implements Iterator<T> {
        private int mod = -1;
        private int nextIdx = 0;

        private void ensureNoComod() {
            if (modCount != mod) throw new ConcurrentModificationException();
        }

        private ReusableIterator reset() {
            mod = modCount;
            iterator = null;
            return this;
        }

        @Override
        public boolean hasNext() {
            ensureNoComod();
            return nextIdx < size;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() throws NoSuchElementException {
            ensureNoComod();
            if (nextIdx >= size) throw new NoSuchElementException();
            return (T) Objects.requireNonNull(data[nextIdx++]);
        }

        public void reuse() {
            mod = -1;
            nextIdx = 0;
            iterator = this;
        }
    }
}
