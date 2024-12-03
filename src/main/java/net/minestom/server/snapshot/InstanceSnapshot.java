package net.minestom.server.snapshot;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Objects;

public sealed interface InstanceSnapshot extends Snapshot, Block.Getter, Biome.Getter, TagReadable
        permits SnapshotImpl.Instance {
    @NotNull DynamicRegistry.Key<DimensionType> dimensionType();

    long worldAge();

    long time();

    @Override
    default @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        ChunkSnapshot chunk = chunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z));
        return Objects.requireNonNull(chunk).getBlock(x, y, z, condition);
    }

    @Override
    default @NotNull DynamicRegistry.Key<Biome> getBiome(int x, int y, int z) {
        ChunkSnapshot chunk = chunk(CoordConversion.globalToChunk(x), CoordConversion.globalToChunk(z));
        return Objects.requireNonNull(chunk).getBiome(x, y, z);
    }

    @Nullable ChunkSnapshot chunk(int chunkX, int chunkZ);

    default @Nullable ChunkSnapshot chunkAt(@NotNull Point point) {
        return chunk(point.chunkX(), point.chunkZ());
    }

    @NotNull Collection<@NotNull ChunkSnapshot> chunks();

    @NotNull Collection<@NotNull EntitySnapshot> entities();

    @NotNull ServerSnapshot server();
}
