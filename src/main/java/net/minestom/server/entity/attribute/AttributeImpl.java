package net.minestom.server.entity.attribute;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

record AttributeImpl(RegistryData.AttributeEntry registry) implements Attribute {
    static final Registry<Attribute> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:attribute"),
            (namespace, properties) -> new AttributeImpl(RegistryData.attribute(namespace, properties)));

    static @UnknownNullability Attribute get(String namespace) {
        return REGISTRY.get(Key.key(namespace));
    }

    @Override
    public String toString() {
        return name();
    }
}
