package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.ServerSnapshot;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.collection.IntMappedArray;
import net.minestom.server.utils.collection.MappedCollection;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.utils.chunk.ChunkUtils.*;

final class InstanceSnapshotImpl {

    record Instance(AtomicReference<ServerSnapshot> serverRef,
                    DimensionType dimensionType, long worldAge, long time,
                    Map<Long, AtomicReference<ChunkSnapshot>> chunksMap,
                    int[] entitiesIds,
                    TagReadable tagReadable) implements InstanceSnapshot {
        @Override
        public @Nullable ChunkSnapshot chunk(int chunkX, int chunkZ) {
            var ref = chunksMap.get(getChunkIndex(chunkX, chunkZ));
            return Objects.requireNonNull(ref, "Chunk not found").getPlain();
        }

        @Override
        public @NotNull Collection<@NotNull ChunkSnapshot> chunks() {
            return MappedCollection.plainReferences(chunksMap.values());
        }

        @Override
        public @NotNull Collection<EntitySnapshot> entities() {
            return new IntMappedArray<>(entitiesIds, id -> server().entity(id));
        }

        @Override
        public @NotNull ServerSnapshot server() {
            return serverRef.getPlain();
        }

        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }
    }

    record Chunk(int minSection, int chunkX, int chunkZ,
                 Section[] sections,
                 Int2ObjectOpenHashMap<Block> blockEntries,
                 int[] entitiesIds,
                 AtomicReference<InstanceSnapshot> instanceRef,
                 TagReadable tagReadable) implements ChunkSnapshot {
        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            // Verify if the block object is present
            if (condition != Condition.TYPE) {
                final Block entry = !blockEntries.isEmpty() ?
                        blockEntries.get(getBlockIndex(x, y, z)) : null;
                if (entry != null || condition == Condition.CACHED) {
                    return entry;
                }
            }
            // Retrieve the block from state id
            final Section section = sections[getChunkCoordinate(y) - minSection];
            final int blockStateId = section.blockPalette()
                    .get(toSectionRelativeCoordinate(x), toSectionRelativeCoordinate(y), toSectionRelativeCoordinate(z));
            return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            final Section section = sections[getChunkCoordinate(y) - minSection];
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

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> entities() {
            return new IntMappedArray<>(entitiesIds, id -> instance().server().entity(id));
        }
    }
}
