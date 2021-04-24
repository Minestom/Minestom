package net.minestom.server.acquirable;

import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AcquirableCollection<E> implements Collection<Acquirable<E>> {

    private final Collection<Acquirable<E>> acquirableCollection;

    public AcquirableCollection(Collection<Acquirable<E>> acquirableCollection) {
        this.acquirableCollection = acquirableCollection;
    }

    public void forEachSync(@NotNull Consumer<E> consumer) {
        Acquisition.acquireForEach(acquirableCollection, consumer);
    }

    public void forEachAsync(@NotNull Consumer<E> consumer) {
        AsyncUtils.runAsync(() -> forEachSync(consumer));
    }

    public @NotNull Stream<E> unwrap() {
        return acquirableCollection.stream().map(Acquirable::unwrap);
    }

    @Override
    public int size() {
        return acquirableCollection.size();
    }

    @Override
    public boolean isEmpty() {
        return acquirableCollection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return acquirableCollection.contains(o);
    }

    @NotNull
    @Override
    public Iterator<Acquirable<E>> iterator() {
        return acquirableCollection.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return acquirableCollection.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return acquirableCollection.toArray(a);
    }

    @Override
    public boolean add(Acquirable<E> eAcquirable) {
        return acquirableCollection.add(eAcquirable);
    }

    @Override
    public boolean remove(Object o) {
        return acquirableCollection.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return acquirableCollection.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Acquirable<E>> c) {
        return acquirableCollection.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return acquirableCollection.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return acquirableCollection.retainAll(c);
    }

    @Override
    public void clear() {
        this.acquirableCollection.clear();
    }
}
