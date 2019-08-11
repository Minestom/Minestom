package fr.themode.minestom.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GroupedCollections<E> implements Iterable<E> {

    private ArrayList<Collection<E>> collections;

    public GroupedCollections() {
        this.collections = new ArrayList<>();
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

    /*public Object[] toArray() {
        return collections.stream().flatMap(Collection::stream).collect(Collectors.toList()).toArray();
    }

    public <T> T[] toArray(T[] ts) {
        return collections.stream().flatMap(Collection::stream).collect(Collectors.toList()).toArray(ts);
    }

    public boolean containsAll(Collection<?> collection) {
        return collections.stream().flatMap(Collection::stream).collect(Collectors.toList()).containsAll(collection);
    }*/

    public void addCollection(Collection<E> list) {
        this.collections.add(list);
    }
}
