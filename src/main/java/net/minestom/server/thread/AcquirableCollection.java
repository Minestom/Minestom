package net.minestom.server.thread;

import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ApiStatus.Experimental
public class AcquirableCollection<E> implements Collection<Acquirable<E>> {
    private final Collection<Acquirable<E>> acquirableCollection;

    public AcquirableCollection(Collection<Acquirable<E>> acquirableCollection) {
        this.acquirableCollection = acquirableCollection;
    }

    public void acquireSync(@NotNull Consumer<E> consumer) {
        final Thread currentThread = Thread.currentThread();
        var threadEntitiesMap = retrieveOptionalThreadMap(acquirableCollection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (var entry : threadEntitiesMap.entrySet()) {
                final TickThread tickThread = entry.getKey();
                final List<E> values = entry.getValue();

                var lock = AcquirableImpl.enter(currentThread, tickThread);
                for (E value : values) {
                    consumer.accept(value);
                }
                AcquirableImpl.leave(lock);
            }
        }
    }

    public void acquireAsync(@NotNull Consumer<E> consumer) {
        AsyncUtils.runAsync(() -> acquireSync(consumer));
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

    /**
     * Creates
     *
     * @param collection    the acquirable collection
     * @param currentThread the current thread
     * @param consumer      the consumer to execute when an element is already in the current thread
     * @return a new Thread to acquirable elements map
     */
    protected static <T> Map<TickThread, List<T>> retrieveOptionalThreadMap(@NotNull Collection<Acquirable<T>> collection,
                                                                            @NotNull Thread currentThread,
                                                                            @NotNull Consumer<T> consumer) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition

        Map<TickThread, List<T>> threadCacheMap = new HashMap<>();
        for (var element : collection) {
            final T value = element.unwrap();

            final TickThread elementThread = element.assignedThread();
            if (currentThread == elementThread) {
                // The element is managed in the current thread, consumer can be immediately called
                consumer.accept(value);
            } else {
                // The element is manager in a different thread, cache it
                List<T> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, tickThread -> new ArrayList<>());
                threadCacheList.add(value);
            }
        }

        return threadCacheMap;
    }

}
