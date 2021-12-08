package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicCollectionView<T> implements Collection<T> {
    private final Collection<AtomicReference<T>> original;

    public AtomicCollectionView(Collection<AtomicReference<T>> original) {
        this.original = original;
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        for (var entry : original) {
            if (entry.get().equals(o)) return true;
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        var iterator = original.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().get();
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        // TODO
        return new Object[0];
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        // TODO
        return null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        if (c.size() > original.size()) return false;
        for (var entry : c) {
            if (!contains(entry)) return false;
        }
        return true;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }
}
