package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to create entity instances.
 * <p>
 * When deserializing an entity from chunk data we can't know if the entity belongs
 * to a custom class/implementation. To make sure the deserialized entity is the same
 * as the original one, custom entity classes should be registered with a factory here.
 * <p>
 * This is ONLY required if you have custom {@link Entity} implementations. If you use
 * the default {@link Entity} class for all entities, you don't have to do anything!
 */
public final class EntityFactories {
    public static final Key DEFAULT_KEY = Key.key("minestom", "entity");
    private static final EntityFactories GLOBAL = new EntityFactories();

    private final EntityFactory fallback = Entity::new;
    private final Map<Key, EntityFactory> factories = new ConcurrentHashMap<>();

    public @NotNull Entity createEntity(@NotNull Key key, @NotNull EntityType entityType, @NotNull UUID uuid) {
        var factory = factories.get(key);
        return Objects.requireNonNullElse(factory, fallback).create(entityType, uuid);
    }

    /**
     * Registers a factory for a given key.
     * <p>
     * This key will be saved during serialization to find the
     * correct factory during deserialization.
     * <p>
     * Every instance created by the factory must return this same key,
     * otherwise there will be issues during deserialization.
     *
     * @param key     the key to register the factory by
     * @param factory the factory to register
     * @throws IllegalArgumentException if {@code key} is {@code minestom:entity}
     * @throws IllegalStateException    if a factory already exists for the {@code key}
     */
    public void registerFactory(@NotNull Key key, @NotNull EntityFactory factory) throws IllegalArgumentException, IllegalStateException {
        if (key.equals(DEFAULT_KEY)) {
            throw new IllegalArgumentException("You can't create a factory for key " + DEFAULT_KEY.asString());
        }
        var previous = this.factories.putIfAbsent(key, factory);
        if (previous != null) {
            throw new IllegalStateException("A factory for key " + key.asString() + " is already registered");
        }
    }

    /**
     * Unregisters a factory.
     *
     * @param key the key to unregister the factory by
     * @throws IllegalStateException if no factory for the {@code key} exists.
     */
    public void unregisterFactory(@NotNull Key key) throws IllegalStateException {
        var removed = this.factories.remove(key);
        if (removed == null) throw new IllegalStateException("There was no factory for key " + key);
    }

    public boolean knowsFactory(@NotNull Key key) {
        return this.factories.containsKey(key);
    }

    /**
     * Adds all factories from the given {@link EntityFactories} to this factory, if not already present.
     *
     * @param other the {@link EntityFactories} to copy from
     */
    public void copyFrom(EntityFactories other) {
        for (var entry : other.factories.entrySet()) {
            this.factories.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return the global {@link EntityFactories}.
     * All {@link Instance}s will by default have this factory.
     */
    public static EntityFactories getGlobal() {
        return GLOBAL;
    }
}
