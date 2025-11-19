package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

public record BlockEntityTypeImpl(Key key, int id) implements BlockEntityType {
    static final Registry<BlockEntityType> REGISTRY = RegistryData.createStaticRegistry(
            BuiltinRegistries.BLOCK_ENTITY_TYPES, BlockEntityTypeImpl::new);

    private BlockEntityTypeImpl(String namespace, RegistryData.Properties properties) {
        this(Key.key(namespace), properties.getInt("id"));
    }

    public static @UnknownNullability BlockEntityType get(RegistryKey<BlockEntityType> key) {
        return REGISTRY.get(key);
    }
}
