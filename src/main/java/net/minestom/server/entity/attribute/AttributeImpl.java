package net.minestom.server.entity.attribute;

import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record AttributeImpl(RegistryData.AttributeEntry registry) implements Attribute {
    static final Registry<Attribute> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.ATTRIBUTE,
            (namespace, properties) -> new AttributeImpl(RegistryData.attribute(namespace, properties)));

    static @UnknownNullability Attribute get(RegistryKey<Attribute> namespace) {
        return REGISTRY.get(namespace);
    }

    @Override
    public String toString() {
        return name();
    }
}
