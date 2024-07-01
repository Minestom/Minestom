package net.minestom.server.entity.attribute;

import net.minestom.server.registry.StaticRegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record AttributeImpl(@NotNull StaticRegistryData.AttributeEntry registry) implements Attribute {
    private static final StaticRegistryData.Container<Attribute> CONTAINER = StaticRegistryData.createStaticContainer(StaticRegistryData.Resource.ATTRIBUTES,
            (namespace, properties) -> new AttributeImpl(StaticRegistryData.attribute(namespace, properties)));

    static Attribute get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Attribute getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Attribute getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Attribute> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
