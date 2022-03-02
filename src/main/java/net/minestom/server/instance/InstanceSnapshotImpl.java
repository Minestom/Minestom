package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.PlayerSnapshot;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.collection.MappedCollection;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.utils.chunk.ChunkUtils.toSectionRelativeCoordinate;

final class InstanceSnapshotImpl {

    record Instance(DimensionType dimensionType, long worldAge, long time,
                    Map<Long, AtomicReference<ChunkSnapshot>> chunksMap,
                    Collection<ChunkSnapshot> chunks,
                    Int2ObjectOpenHashMap<AtomicReference<EntitySnapshot>> entitiesMap,
                    Collection<EntitySnapshot> entities,
                    Long2ObjectOpenHashMap<List<AtomicReference<EntitySnapshot>>> chunkEntities,
                    Int2ObjectOpenHashMap<AtomicReference<PlayerSnapshot>> playersMap,
                    Collection<PlayerSnapshot> players,
                    Long2ObjectOpenHashMap<List<AtomicReference<PlayerSnapshot>>> chunkPlayers,
                    TagReadable tagReadable) implements InstanceSnapshot {
        @Override
        public @Nullable ChunkSnapshot chunk(int chunkX, int chunkZ) {
            var ref = chunksMap.get(ChunkUtils.getChunkIndex(chunkX, chunkZ));
            return Objects.requireNonNull(ref, "Chunk not found").getPlain();
        }

        @Override
        public @UnknownNullability EntitySnapshot entity(int entityId) {
            return entitiesMap.get(entityId).getPlain();
        }

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> chunkEntities(int chunkX, int chunkZ) {
            return MappedCollection.plainReferences(chunkEntities.get(ChunkUtils.getChunkIndex(chunkX, chunkZ)));
        }

        @Override
        public @UnknownNullability PlayerSnapshot player(int playerId) {
            return playersMap.get(playerId).getPlain();
        }

        @Override
        public @NotNull Collection<@NotNull PlayerSnapshot> chunkPlayers(int chunkX, int chunkZ) {
            return MappedCollection.plainReferences(chunkPlayers.get(ChunkUtils.getChunkIndex(chunkX, chunkZ)));
        }

        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }
    }

    record Chunk(int minSection, int chunkX, int chunkZ,
                 Section[] sections,
                 Int2ObjectOpenHashMap<Block> blockEntries,
                 AtomicReference<InstanceSnapshot> instanceRef,
                 TagReadable tagReadable) implements ChunkSnapshot {
        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            // Verify if the block object is present
            if (condition != Condition.TYPE) {
                final Block entry = !blockEntries.isEmpty() ?
                        blockEntries.get(ChunkUtils.getBlockIndex(x, y, z)) : null;
                if (entry != null || condition == Condition.CACHED) {
                    return entry;
                }
            }
            // Retrieve the block from state id
            final Section section = sections[ChunkUtils.getChunkCoordinate(y) - minSection];
            final int blockStateId = section.blockPalette()
                    .get(toSectionRelativeCoordinate(x), toSectionRelativeCoordinate(y), toSectionRelativeCoordinate(z));
            return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            final Section section = sections[ChunkUtils.getChunkCoordinate(y) - minSection];
            final int id = section.biomePalette()
                    .get(toSectionRelativeCoordinate(x) / 4, toSectionRelativeCoordinate(y) / 4, toSectionRelativeCoordinate(z) / 4);
            return MinecraftServer.getBiomeManager().getById(id);
        }

        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }

        @Override
        public @NotNull InstanceSnapshot instance() {
            return instanceRef.getPlain();
        }
    }
}
