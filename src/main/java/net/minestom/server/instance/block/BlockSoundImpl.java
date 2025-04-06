package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public record BlockSoundImpl(RegistryData.BlockSoundTypeEntry registry) implements BlockSoundType {
    static final StaticRegistry<BlockSoundType> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.BLOCK_SOUND_TYPES, "minecraft:block_sound_type",
            (namespace, properties) -> new BlockSoundImpl(RegistryData.blockSoundTypeEntry(namespace, properties)));

    static @UnknownNullability BlockSoundType get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

}
