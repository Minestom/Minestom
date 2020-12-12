package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class NamespaceIDHashMap<V> extends AbstractMap<NamespaceID, V> {

    private final Map<NamespaceID, V> backing = new HashMap<>();

    @NotNull
    @Override
    public Set<Entry<NamespaceID, V>> entrySet() {
        return backing.entrySet();
    }

    @Override
    public V get(Object key) {
        return backing.get(key);
    }

    @Override
    public V put(NamespaceID key, V value) {
        return backing.put(key, value);
    }

    public boolean containsKey(String id) {
        return containsKey(NamespaceID.getDomain(id), NamespaceID.getPath(id));
    }

    public boolean containsKey(String domain, String path) {
        return backing.containsKey(NamespaceID.from(domain, path));
    }

    public V get(String id) {
        return get(NamespaceID.getDomain(id), NamespaceID.getPath(id));
    }

    public V get(String domain, String path) {
        return backing.get(NamespaceID.from(domain, path));
    }

    public V put(String domain, String path, V value) {
        return put(NamespaceID.from(domain, path), value);
    }

    public V computeIfAbsent(String domain, String path, Function<? super NamespaceID, ? extends V> mappingFunction) {
        return computeIfAbsent(NamespaceID.from(domain, path), mappingFunction);
    }

    public V put(String id, V value) {
        return put(NamespaceID.from(id), value);
    }

    public V computeIfAbsent(String id, Function<? super NamespaceID, ? extends V> mappingFunction) {
        return computeIfAbsent(NamespaceID.from(id), mappingFunction);
    }

    public V getOrDefault(String id, V defaultValue) {
        return getOrDefault(NamespaceID.from(id), defaultValue);
    }

    public V getOrDefault(String domain, String path, V defaultValue) {
        return getOrDefault(NamespaceID.from(domain, path), defaultValue);
    }
}
