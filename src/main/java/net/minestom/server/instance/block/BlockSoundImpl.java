package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.UnknownNullability;

public record BlockSoundImpl(RegistryData.BlockSoundTypeEntry registry) implements BlockSoundType {
    static final Registry<BlockSoundType> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:block_sound_type"),
            (namespace, properties) -> new BlockSoundImpl(RegistryData.blockSoundTypeEntry(namespace, properties)));

    static @UnknownNullability BlockSoundType get(String key) {
        return REGISTRY.get(Key.key(key));
    }

}
