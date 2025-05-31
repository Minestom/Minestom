package net.minestom.server.instance.block;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record BlockSoundImpl(Registry.BlockSoundTypeEntry registry) implements BlockSoundType {
    private static final Registry.Container<BlockSoundType> CONTAINER = Registry.createStaticContainer(Registry.Resource.BLOCK_SOUND_TYPES,
            (namespace, properties) -> new BlockSoundImpl(Registry.blockSoundTypeEntry(namespace, properties)));

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
