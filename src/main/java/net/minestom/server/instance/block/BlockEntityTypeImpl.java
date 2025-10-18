package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

public record BlockEntityTypeImpl(Key key, int id) implements BlockEntityType {
    static final Registry<BlockEntityType> REGISTRY = RegistryData.createStaticRegistry(
            Key.key("block_entity_type"), BlockEntityTypeImpl::new);

    private BlockEntityTypeImpl(String namespace, RegistryData.Properties properties) {
        this(Key.key(namespace), properties.getInt("id"));
    }

    public static @UnknownNullability BlockEntityType get(String key) {
        return REGISTRY.get(Key.key(key));
    }
}
