package net.minestom.server.snapshot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Objects;

import static net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordinate;

public interface InstanceSnapshot extends Snapshot, Block.Getter, Biome.Getter, TagReadable {
    @NotNull DimensionType dimensionType();

    long worldAge();

    long time();

    @Override
    default @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        ChunkSnapshot chunk = chunk(getChunkCoordinate(x), getChunkCoordinate(z));
        return Objects.requireNonNull(chunk).getBlock(x, y, z, condition);
    }

    @Override
    default @NotNull Biome getBiome(int x, int y, int z) {
        ChunkSnapshot chunk = chunk(getChunkCoordinate(x), getChunkCoordinate(z));
        return Objects.requireNonNull(chunk).getBiome(x, y, z);
    }

    @Nullable ChunkSnapshot chunk(int chunkX, int chunkZ);

    default @Nullable ChunkSnapshot chunkAt(@NotNull Point point) {
        return chunk(point.chunkX(), point.chunkZ());
    }

    @NotNull Collection<@NotNull ChunkSnapshot> chunks();

    @UnknownNullability EntitySnapshot entity(int entityId);

    @NotNull Collection<@NotNull EntitySnapshot> chunkEntities(int chunkX, int chunkZ);

    @NotNull Collection<@NotNull EntitySnapshot> entities();

    @UnknownNullability PlayerSnapshot player(int playerId);

    @NotNull Collection<@NotNull PlayerSnapshot> chunkPlayers(int chunkX, int chunkZ);

    @NotNull Collection<@NotNull PlayerSnapshot> players();
}
