package net.minestom.server.attribute;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents a {@link net.minestom.server.entity.LivingEntity living entity} attribute.
 */
public record AttributeImpl(Registry.AttributeEntry registry, int id) implements Attribute, ProtocolObject {

    private static final Registry.DynamicContainer<Attribute> CONTAINER = Registry.createDynamicContainer(Registry.Resource.ATTRIBUTES, AttributeImpl::createImpl);

    private static Attribute createImpl(String namespace, Registry.Properties properties) {
        Registry.AttributeEntry attributeEntry = Registry.attribute(namespace, properties);
        return new AttributeImpl(attributeEntry, attributeEntry.id());
    }

    static Collection<Attribute> values() {
        return CONTAINER.values();
    }

    static Attribute get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Attribute getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return this.registry.namespace();
    }

    @Override
    public float defaultValue() {
        return this.registry.defaultValue();
    }

    @Override
    public float maxValue() {
        return this.registry.maxValue();
    }

    @Override
    public float mineValue() {
        return this.registry.maxValue();
    }

    @Override
    public boolean clientSync() {
        return this.registry.clientSync();
    }

    @Override
    public @NotNull String translationKey() {
        return this.registry.translationKey();
    }
}
