package net.minestom.server.attribute;

import java.util.Collection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeManager {

    private final Map<String, Attribute> attributes = new ConcurrentHashMap<>();

    /**
     * Register this attribute.
     *
     * @see #fromKey(String)
     * @see #values()
     */
    public synchronized void register(Attribute attribute) {
        attributes.put(attribute.key(), attribute);
    }

    /**
     * Retrieves an attribute by its key.
     *
     * @param key the key of the attribute
     * @return the attribute for the key or null if not any
     */
    public synchronized @Nullable Attribute fromKey(@NotNull String key) {
        return attributes.get(key);
    }

    /**
     * Retrieves all registered attributes.
     *
     * @return an array containing all registered attributes
     */
    public synchronized @NotNull Collection<@NotNull Attribute> values() {
        return attributes.values();
    }
}
