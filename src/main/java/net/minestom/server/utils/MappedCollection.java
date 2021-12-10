package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@ApiStatus.Internal
public record MappedCollection<O, R>(@NotNull Collection<O> original,
                                     @NotNull Function<O, R> mapper) implements Collection<R> {
    public static <O extends AtomicReference<R>, R> MappedCollection<O, R> plainReferences(@NotNull Collection<O> original) {
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
    public @NotNull Iterator<R> iterator() {
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
    public @NotNull Object @NotNull [] toArray() {
        // TODO
        throw new UnsupportedOperationException("Unsupported array object");
    }

    @Override
    public <T> @NotNull T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        // TODO
        throw new UnsupportedOperationException("Unsupported array generic");
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
    public boolean add(R t) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unmodifiable collection");
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends R> c) {
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
