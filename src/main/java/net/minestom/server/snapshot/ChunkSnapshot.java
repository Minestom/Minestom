package net.minestom.server.snapshot;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

public interface ChunkSnapshot extends Snapshot, Block.Getter, Biome.Getter, TagReadable {
    int chunkX();

    int chunkZ();

    @NotNull InstanceSnapshot instance();
}
