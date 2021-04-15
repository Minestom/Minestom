package net.minestom.server.utils.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * A CollectionView is a class which is mapped to another collection
 * and convert every result using a given function. It is more efficient
 * than filling a new collection every time, as long as the two types are interchangeable.
 * <p>
 * The view is not thread-safe.
 *
 * @param <E> the type that the collection should return
 * @param <V> the type of the viewed collection
 */
public class CollectionView<E, V> implements Collection<E> {

    private final Collection<V> collectionView;
    private final Function<E, V> toViewFunction;
    private final Function<V, E> toTypeFunction;

    public CollectionView(@NotNull Collection<V> collectionView,
                          @NotNull Function<E, V> toViewFunction,
                          @NotNull Function<V, E> toTypeFunction) {
        this.collectionView = collectionView;
        this.toViewFunction = toViewFunction;
        this.toTypeFunction = toTypeFunction;
    }

    @Override
    public int size() {
        return collectionView.size();
    }

    @Override
    public boolean isEmpty() {
        return collectionView.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        try {
            return collectionView.contains(toViewFunction.apply((E) o));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new IteratorView<>(collectionView.iterator(), toTypeFunction);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];

        int i = 0;
        for (E e : this) {
            array[i++] = e;
        }

        return array;
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        int i = 0;
        for (E e : this) {
            a[i++] = (T1) e;
        }

        return a;
    }

    @Override
    public boolean add(E e) {
        return collectionView.add(toViewFunction.apply(e));
    }

    @Override
    public boolean remove(Object o) {
        try {
            return collectionView.remove(toViewFunction.apply((E) o));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object e : c) {
            if (!contains(e))
                return false;
        }

        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean changed = false;
        try {
            for (Object e : c) {
                if (add((E) e))
                    changed = true;
            }
        } catch (ClassCastException ignored) {
        }

        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean changed = false;
        try {
            for (Object e : c) {
                if (remove(e))
                    changed = true;
            }
        } catch (ClassCastException ignored) {
        }

        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = false;
        try {
            for (Object e : c) {
                if (!contains(e)) {
                    remove(e);
                    changed = true;
                }
            }
        } catch (ClassCastException ignored) {
        }

        return changed;
    }

    @Override
    public void clear() {
        this.collectionView.clear();
    }

    public static class IteratorView<T, V> implements Iterator<T> {

        private final Iterator<V> iteratorView;
        private final Function<V, T> toTypeFunction;

        public IteratorView(Iterator<V> iteratorView,
                            Function<V, T> toTypeFunction) {
            this.iteratorView = iteratorView;
            this.toTypeFunction = toTypeFunction;
        }

        @Override
        public boolean hasNext() {
            return iteratorView.hasNext();
        }

        @Override
        public T next() {
            final V viewElement = iteratorView.next();
            return toTypeFunction.apply(viewElement);
        }
    }

}