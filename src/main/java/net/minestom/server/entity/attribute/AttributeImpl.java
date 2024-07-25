package net.minestom.server.entity.attribute;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record AttributeImpl(@NotNull Registry.AttributeEntry registry) implements Attribute {
    private static final Registry.Container<Attribute> CONTAINER = Registry.createStaticContainer(Registry.Resource.ATTRIBUTES,
            (key, properties) -> new AttributeImpl(Registry.attribute(key, properties)));

    static Attribute get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static Attribute getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
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
