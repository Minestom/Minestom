package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.collection.IntMappedArray;
import net.minestom.server.utils.collection.MappedCollection;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.minestom.server.coordinate.CoordConversion.*;

@ApiStatus.Internal
public final class SnapshotImpl {
    public record Server(Collection<InstanceSnapshot> instances,
                         Int2ObjectOpenHashMap<AtomicReference<EntitySnapshot>> entityRefs) implements ServerSnapshot {
        @Override
        public @NotNull Collection<EntitySnapshot> entities() {
            return MappedCollection.plainReferences(entityRefs.values());
        }

        @Override
        public @UnknownNullability EntitySnapshot entity(int id) {
            var ref = entityRefs.get(id);
            return ref != null ? ref.getPlain() : null;
        }
    }

    public record Instance(AtomicReference<ServerSnapshot> serverRef,
                           DynamicRegistry.Key<DimensionType> dimensionType, long worldAge, long time,
                           Map<Long, AtomicReference<ChunkSnapshot>> chunksMap,
                           int[] entitiesIds,
                           TagReadable tagReadable) implements InstanceSnapshot {
        @Override
        public @Nullable ChunkSnapshot chunk(int chunkX, int chunkZ) {
            var ref = chunksMap.get(chunkIndex(chunkX, chunkZ));
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
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }
    }

    public record Chunk(int minSection, int chunkX, int chunkZ,
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
                        blockEntries.get(chunkBlockIndex(x, y, z)) : null;
                if (entry != null || condition == Condition.CACHED) {
                    return entry;
                }
            }
            // Retrieve the block from state id
            final Section section = sections[globalToChunk(y) - minSection];
            final int blockStateId = section.blockPalette()
                    .get(globalToSectionRelative(x), globalToSectionRelative(y), globalToSectionRelative(z));
            return Objects.requireNonNullElse(Block.fromStateId((short) blockStateId), Block.AIR);
        }

        @Override
        public @NotNull DynamicRegistry.Key<Biome> getBiome(int x, int y, int z) {
            final Section section = sections[globalToChunk(y) - minSection];
            final int id = section.biomePalette()
                    .get(globalToSectionRelative(x) / 4, globalToSectionRelative(y) / 4, globalToSectionRelative(z) / 4);
            DynamicRegistry.Key<Biome> key = MinecraftServer.getBiomeRegistry().getKey(id);
            Check.notNull(key, "Biome with id {0} is not registered", id);
            return key;
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
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

    public record Entity(EntityType type, UUID uuid, int id, Pos position, Vec velocity,
                         AtomicReference<InstanceSnapshot> instanceRef, int chunkX, int chunkZ,
                         int[] viewersId, int[] passengersId, int vehicleId,
                         TagReadable tagReadable) implements EntitySnapshot {
        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }

        @Override
        public @NotNull InstanceSnapshot instance() {
            return instanceRef.getPlain();
        }

        @Override
        public @NotNull ChunkSnapshot chunk() {
            return Objects.requireNonNull(instance().chunk(chunkX, chunkZ));
        }

        @Override
        public @NotNull Collection<@NotNull PlayerSnapshot> viewers() {
            return new IntMappedArray<>(viewersId, id -> (PlayerSnapshot) instance().server().entity(id));
        }

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> passengers() {
            return new IntMappedArray<>(passengersId, id -> instance().server().entity(id));
        }

        @Override
        public @Nullable EntitySnapshot vehicle() {
            if (vehicleId == -1) return null;
            return instance().server().entity(vehicleId);
        }
    }

    public record Player(EntitySnapshot snapshot, String username,
                         GameMode gameMode) implements PlayerSnapshot {
        @Override
        public @NotNull EntityType type() {
            return snapshot.type();
        }

        @Override
        public @NotNull UUID uuid() {
            return snapshot.uuid();
        }

        @Override
        public int id() {
            return snapshot.id();
        }

        @Override
        public @NotNull Pos position() {
            return snapshot.position();
        }

        @Override
        public @NotNull Vec velocity() {
            return snapshot.velocity();
        }

        @Override
        public @NotNull InstanceSnapshot instance() {
            return snapshot.instance();
        }

        @Override
        public @NotNull ChunkSnapshot chunk() {
            return snapshot.chunk();
        }

        @Override
        public @NotNull Collection<@NotNull PlayerSnapshot> viewers() {
            return snapshot.viewers();
        }

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> passengers() {
            return snapshot.passengers();
        }

        @Override
        public @Nullable EntitySnapshot vehicle() {
            return snapshot.vehicle();
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return snapshot.getTag(tag);
        }
    }
}
