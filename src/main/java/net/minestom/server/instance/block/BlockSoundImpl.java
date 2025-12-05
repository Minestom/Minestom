package net.minestom.server.instance.block;

import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.UnknownNullability;

record BlockSoundImpl(RegistryData.BlockSoundTypeEntry registry) implements BlockSoundType {
    static final Registry<BlockSoundType> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.BLOCK_SOUND_TYPE,
            (namespace, properties) -> new BlockSoundImpl(RegistryData.blockSoundTypeEntry(namespace, properties)));

    static @UnknownNullability BlockSoundType get(RegistryKey<BlockSoundType> key) {
        return REGISTRY.get(key);
    }
}
