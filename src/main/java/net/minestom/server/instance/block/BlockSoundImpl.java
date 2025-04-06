package net.minestom.server.instance.block;

import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record BlockSoundImpl(RegistryData.BlockSoundTypeEntry registry) implements BlockSoundType {
    private static final RegistryData.Container<BlockSoundType> CONTAINER = RegistryData.createStaticContainer(RegistryData.Resource.BLOCK_SOUND_TYPES,
            (namespace, properties) -> new BlockSoundImpl(RegistryData.blockSoundTypeEntry(namespace, properties)));

    static BlockSoundType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static BlockSoundType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Collection<BlockSoundType> values() {
        return CONTAINER.values();
    }
}
