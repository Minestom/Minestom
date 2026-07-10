package net.minestom.server.utils.collection;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@ApiStatus.Internal
public record MappedCollection<O, R>(Collection<O> original,
                                     Function<O, R> mapper) implements Collection<R> {
    public static <O extends AtomicReference<R>, R> MappedCollection<O, R> plainReferences(Collection<O> original) {
        return new MappedCollection<>(original, AtomicReference::getPlain);
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
            if (mapper.apply(entry).equals(o)) return true;
        }
        return false;
    }

    @Override
    public Iterator<R> iterator() {
        var iterator = original.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return mapper.apply(iterator.next());
            }
        };
    }

    @Override
    public Object [] toArray() {
        // TODO
        throw new UnsupportedOperationException("Unsupported array object");
    }

    @Override
    public <T> T [] toArray(T [] a) {
        // TODO
        throw new UnsupportedOperationException("Unsupported array generic");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c.size() > original.size()) return false;
        for (var entry : c) {
            if (!contains(entry)) return false;
        }
        return true;
    }

    @Override
    public boolean add(R t) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean addAll(Collection<? extends R> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }
}
