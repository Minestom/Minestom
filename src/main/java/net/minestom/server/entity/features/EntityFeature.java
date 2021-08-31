package net.minestom.server.entity.features;

import net.minestom.server.entity.Entity;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class EntityFeature<T extends EntityFeatureBase> {

    private static AtomicInteger ID = new AtomicInteger();

    private final int id;
    private final Function<Entity, T> constructor;
    private final Set<EntityFeature<?>> dependencies;

    public EntityFeature(Function<Entity, T> constructor, Set<EntityFeature<?>> dependencies) {
        this.id = ID.getAndIncrement();
        this.constructor = constructor;
        this.dependencies = Collections.unmodifiableSet(dependencies);
    }

    public EntityFeature(Function<Entity, T> constructor) {
        this(constructor, Collections.emptySet());
    }

    public int getId() {
        return this.id;
    }

    public T getDefaultImplementationFor(Entity entity) {
        return constructor.apply(entity);
    }

    public Set<EntityFeature<?>> getDependencies() {
        return dependencies;
    }

}
