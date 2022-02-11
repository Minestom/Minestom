package net.minestom.server.utils.collection;

import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApiStatus.Internal
public final class MergedMap<K, V> extends AbstractMap<K, V> {
    private final Map<K, V> first, second;

    public MergedMap(Map<K, V> first, Map<K, V> second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    // mandatory methods

    final Set<Entry<K, V>> entrySet = new AbstractSet<>() {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return stream().iterator();
        }

        @Override
        public int size() {
            return (int) stream().count();
        }

        @Override
        public Stream<Entry<K, V>> stream() {
            return Stream.concat(first.entrySet().stream(), secondStream())
                    .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue()));
        }

        @Override
        public Stream<Entry<K, V>> parallelStream() {
            return stream().parallel();
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return stream().spliterator();
        }
    };

    Stream<Entry<K, V>> secondStream() {
        return second.entrySet().stream().filter(e -> !first.containsKey(e.getKey()));
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet;
    }

    // optimizations

    @Override
    public boolean containsKey(Object key) {
        return first.containsKey(key) || second.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return first.containsValue(value) ||
                secondStream().anyMatch(Predicate.isEqual(value));
    }

    @Override
    public V get(Object key) {
        V v = first.get(key);
        return v != null ? v : second.get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V v = first.get(key);
        return v != null ? v : second.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        first.forEach(action);
        second.forEach((k, v) -> {
            if (!first.containsKey(k)) action.accept(k, v);
        });
    }
}
