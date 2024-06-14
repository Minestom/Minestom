package net.minestom.server.snapshot;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public sealed interface ChunkSnapshot extends Snapshot, Block.Getter, Biome.Getter, TagReadable
        permits SnapshotImpl.Chunk {
    int chunkX();

    int chunkZ();

    @NotNull InstanceSnapshot instance();

    @NotNull Collection<@NotNull EntitySnapshot> entities();
}
