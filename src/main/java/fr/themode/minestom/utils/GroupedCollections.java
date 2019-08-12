package fr.themode.minestom.utils;

import java.util.Collection;
import java.util.Iterator;

public class GroupedCollections<E> implements Iterable<E> {

    private Collection<Collection<E>> collections;

    public GroupedCollections(Collection<Collection<E>> collection) {
        this.collections = collection;
    }

    public int size() {
        return collections.stream().mapToInt(es -> es.size()).sum();
    }

    public boolean isEmpty() {
        return collections.stream().allMatch(es -> es.isEmpty());
    }

    public boolean contains(Object o) {
        return collections.stream().anyMatch(es -> es.contains(o));
    }

    @Override
    public Iterator<E> iterator() {
        return collections.stream().flatMap(Collection::stream).iterator();
    }

    public void addCollection(Collection<E> list) {
        this.collections.add(list);
    }
}
