package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.UpdateLightPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.SnapshotImpl;
import net.minestom.server.snapshot.SnapshotUpdater;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class InstanceContainer extends InstanceBase {

    // World data
    private final Long2ObjectMap<PaletteSectionData> sectionData = Long2ObjectSyncMap.hashmap();
    private final Long2ObjectMap<SectionCache> sectionCache = Long2ObjectSyncMap.hashmap();

    // Chunk views
    private final Long2ObjectMap<Chunk> chunkViews = Long2ObjectSyncMap.hashmap();

    // Generation data
    private final Long2ObjectMap<Queue<GeneratorImpl.SectionModifierImpl>> generationForks = Long2ObjectSyncMap.hashmap();
    private final LongSet chunksToUpdate = Long2ObjectSyncMap.hashset();

    public InstanceContainer(UUID uuid, DimensionType dimensionType) {
        super(uuid, dimensionType);
    }

    private @Nullable PaletteSectionData getData(long index) {
        return sectionData.get(index);
    }

    public @Nullable Section getSection(long index) {
        PaletteSectionData data = getData(index);
        return data == null ? null : data.sectionView();
    }

    public @Nullable SectionCache getSectionCache(long index) {
        if (getSection(index) == null) {
            return null;
        }
        return sectionCache.computeIfAbsent(index, i -> {
            int chunkX = ChunkUtils.getSectionCoordX(i);
            int sectionY = ChunkUtils.getSectionCoordY(i);
            int chunkZ = ChunkUtils.getSectionCoordZ(i);
            return new SectionCache(getSection(index), chunkX, sectionY, chunkZ);
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return chunkLoader.saveInstance(this);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        return chunkLoader.saveChunks(chunkViews);
    }

    @Override
    public @Nullable Block retrieveBlock(int x, int y, int z, @NotNull Condition condition) {
        long index = ChunkUtils.getSectionIndex(x, y, z);
        Section section = getSection(index);
        if (section == null) {
            throw new IllegalStateException("No section at " + x + ", " + y + ", " + z);
        }
        return section.getBlock(x, y, z);
    }

    @Override
    public @NotNull InstanceSnapshot updateSnapshot(@NotNull SnapshotUpdater updater) {
        // TODO: Instance snapshot using sections instead of chunks
        final Map<Long, AtomicReference<ChunkSnapshot>> chunksMap = updater.referencesMapLong(chunkViews.values(),
                chunk -> chunkViews.long2ObjectEntrySet()
                .stream()
                .filter(entry -> entry.getValue() == chunk)
                .findAny()
                .orElseThrow()
                .getLongKey());
        final int[] entities = ArrayUtils.mapToIntArray(entityTracker.entities(), Entity::getEntityId);
        return new SnapshotImpl.Instance(updater.reference(MinecraftServer.process()),
                getDimensionType(), getWorldAge(), getTime(), chunksMap, entities,
                tagHandler.readableCopy());
    }

    @Override
    public void refreshCurrentChunk(Tickable tickable, int newChunkX, int newChunkZ) {

    }

    @Override
    public void registerDispatcher(ThreadDispatcher<SectionCache> dispatcher) {
        getLoadedSections().keySet()
                .longStream()
                .mapToObj(this::getSectionCache)
                .filter(Objects::nonNull)
                .forEach(dispatcher::createPartition);

    }

    @Override
    public ByteList getSkyLight(int chunkX, int sectionY, int chunkZ) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        Section section = getSection(index);
        if (section == null) throw new IllegalStateException("No section at " + chunkX + ", " + sectionY + ", " + chunkZ);
        return section.getSkyLight();
    }

    @Override
    public ByteList getBlockLight(int chunkX, int sectionY, int chunkZ) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        Section section = getSection(index);
        if (section == null) throw new IllegalStateException("No section at " + chunkX + ", " + sectionY + ", " + chunkZ);
        return section.getBlockLight();
    }

    @Override
    public void setSkyLight(int chunkX, int sectionY, int chunkZ, ByteList light) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        Section section = getSection(index);
        if (section == null) throw new IllegalStateException("No section at " + chunkX + ", " + sectionY + ", " + chunkZ);
        section.setSkyLight(light);
    }

    @Override
    public void setBlockLight(int chunkX, int sectionY, int chunkZ, ByteList light) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        Section section = getSection(index);
        if (section == null) throw new IllegalStateException("No section at " + chunkX + ", " + sectionY + ", " + chunkZ);
        section.setBlockLight(light);
    }

    public @NotNull CompletableFuture<Void> unloadSection(int chunkX, int sectionY, int chunkZ) {
        long sectionIndex = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        sectionData.remove(sectionIndex);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull Long2ObjectMap<Chunk> getLoadedChunks() {
        return Long2ObjectMaps.unmodifiable(chunkViews);
    }

    @Override
    public boolean isSectionLoaded(int chunkX, int sectionY, int chunkZ) {
        long sectionIndex = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        return getSection(sectionIndex) != null;
    }

    @Override
    public Long2ObjectMap<Section> getLoadedSections() {
        Long2ObjectMap<Section> map = new Long2ObjectOpenHashMap<>();
        sectionData.long2ObjectEntrySet()
                .forEach(entry -> map.put(entry.getLongKey(), entry.getValue().sectionView()));
        return Long2ObjectMaps.unmodifiable(map);
    }

    @Override
    public CompletableFuture<Section> loadSection(int chunkX, int sectionY, int chunkZ) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        Section section = getSection(index);
        if (section != null) {
            return CompletableFuture.completedFuture(section);
        }

        // Generate a new section/load from storage
        return loadChunkFromStorage(chunkX, chunkZ).thenCompose((ignored) -> {
            Section newSection = getSection(index);
            if (newSection != null) return CompletableFuture.completedFuture(newSection);
            return generateSection(chunkX, sectionY, chunkZ);
        });
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        Section section = getSection(ChunkUtils.getSectionIndex(x, y, z));
        if (section == null) throw new IllegalStateException("No section at " + x + ", " + y + ", " + z);
        section.setBlock(x, y, z, block);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        Section section = getSection(ChunkUtils.getSectionIndex(x, y, z));
        if (section == null) throw new IllegalStateException("No section at " + x + ", " + y + ", " + z);
        section.setBiome(x, y, z, biome);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        Section section = getSection(ChunkUtils.getSectionIndex(x, y, z));
        if (section == null) throw new IllegalStateException("No section at " + x + ", " + y + ", " + z);
        return section.getBiome(x, y, z);
    }

    private CompletableFuture<Void> loadChunkFromStorage(int chunkX, int chunkZ) {
        // Load chunk from storage
        if (chunkLoader == null) return AsyncUtils.VOID_FUTURE;
        return chunkLoader.loadChunk(this, chunkX, chunkZ).thenAccept(chunk -> {
            // TODO: Apply the chunk to this instance. chunkbatch?
        });
    }

    private CompletableFuture<Section> generateSection(int chunkX, int sectionY, int chunkZ) {
        long index = ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ);
        return CompletableFuture.supplyAsync(() -> {
            PaletteSectionData data = new PaletteSectionData();
            Section section = data.sectionView();

            GeneratorImpl.UnitImpl unit = (GeneratorImpl.UnitImpl) GeneratorImpl.section(section, chunkX, sectionY, chunkZ);
            generator.generate(unit);

            // Register forks or apply locally
            for (var fork : unit.forks()) {

                // Only support native minestom section modifiers
                if (!(fork.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier)) {
                    throw new UnsupportedOperationException("Unsupported fork modifier: " + fork.modifier());
                }

                final Point start = fork.absoluteStart();
                long sectionIndex = ChunkUtils.getSectionIndex(start.chunkX(), start.section(), start.chunkZ());
                final Section forkSection = getSection(sectionIndex);

                if (forkSection != null) {
                    // Fork chunk exists, we can apply the section now
                    applyModifier(forkSection, sectionModifier);

                    // Update players
                    chunksToUpdate.add(ChunkUtils.getChunkIndex(start.chunkX(), start.chunkZ()));
                } else {
                    // Fork chunk does not exist, add it to the future forks
                    this.generationForks.computeIfAbsent(sectionIndex,
                            (ignored) -> new ArrayDeque<>()).add(sectionModifier);
                }
            }

            return data;
        }, ForkJoinPool.commonPool())
                .thenApply(data -> {
            // Set section
            Section section = data.sectionView();

            // Apply previous forks
            var forks = generationForks.get(index);
            if (forks == null) return data;
            for (var fork : forks) {
                applyModifier(section, fork);
            }
            generationForks.remove(index);

            return data;
        })
                .thenApply(data -> {
            sectionData.put(index, data);
            return data.sectionView();
        });
    }

    private void applyModifier(Section section, GeneratorImpl.SectionModifierImpl modifier) {
        modifier.section().forEachBlock(section::setBlock);
        modifier.section().forEachBiome(section::setBiome);
        section.setSkyLight(modifier.section().getSkyLight());
        section.setBlockLight(modifier.section().getBlockLight());
    }

    public void addSharedInstance(SharedInstance sharedInstance) {
        // TODO:
    }

    public Collection<SharedInstance> getSharedInstances() {
        // TODO:
        return null;
    }

    private class ChunkView implements Chunk {

        private final int chunkX;
        private final int chunkZ;

        private final int minSection = getMinSection();
        private final int maxSection = getMaxSection();

        private final UUID uuid;

        // Mutable data/cache
        private boolean readOnly = false;
        private final TagHandler tagHandler = TagHandler.newHandler();
        private volatile ChunkDataPacket lastPacket = null;

        // Pathfinding
        private PFColumnarSpace columnarSpace = new PFColumnarSpace(new PFInstanceSpace(InstanceContainer.this), this);

        private ChunkView(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            {
                long first = 0;
                first = 31 * first + InstanceContainer.this.getUniqueId().getLeastSignificantBits();
                first = 31 * first + InstanceContainer.this.getUniqueId().getMostSignificantBits();
                long second = ChunkUtils.getChunkIndex(chunkX, chunkZ);
                this.uuid = new UUID(first, second);
            }
        }

        @Override
        public @Nullable Section getSection(int section) {
            return InstanceContainer.this.getSection(chunkX, section, chunkZ);
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            int sectionY = y / Section.SIZE_Y;
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            section.setBlock(x, y, z, block);
        }

        @Override
        public void reset() {
            for (int sectionY = minSection; sectionY < maxSection; sectionY++) {
                Section section = getSection(sectionY);
                if (section == null) continue;
                section.clear();
            }
        }

        @Override
        public @NotNull UUID getIdentifier() {
            return uuid;
        }

        @Override
        public int getMinSection() {
            return minSection;
        }

        @Override
        public int getMaxSection() {
            return maxSection;
        }

        @Override
        public boolean isReadOnly() {
            return readOnly;
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        @Override
        public void setColumnarSpace(PFColumnarSpace columnarSpace) {
            this.columnarSpace = columnarSpace;
        }

        @Override
        public boolean isLoaded() {
            return chunkViews.containsKey(ChunkUtils.getChunkIndex(chunkX, chunkZ));
        }

        @Override
        public CompletableFuture<Void> unload() {
            return CompletableFuture.allOf(IntStream.range(minSection, maxSection)
                    .mapToObj(sectionY -> InstanceContainer.this.unloadSection(chunkX, sectionY, chunkZ))
                    .toArray(CompletableFuture[]::new));
        }

        private boolean hasChanged() {
            return IntStream.range(minSection, maxSection)
                    .mapToLong(sectionY -> ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ))
                    .mapToObj(InstanceContainer.this::getData)
                    .filter(Objects::nonNull)
                    .anyMatch(PaletteSectionData::hasChanged);
        }

        private void acknowledgeChanges() {
            IntStream.range(minSection, maxSection)
                    .mapToLong(sectionY -> ChunkUtils.getSectionIndex(chunkX, sectionY, chunkZ))
                    .mapToObj(InstanceContainer.this::getData)
                    .filter(Objects::nonNull)
                    .forEach(PaletteSectionData::acknowledgeChanges);
        }

        @Override
        public ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
            if (lastPacket != null) {
                if (!hasChanged()) {
                    return lastPacket;
                }
            }
            ChunkDataPacket packet = Chunk.super.chunkPacket(chunkX, chunkZ);
            lastPacket = packet;
            acknowledgeChanges();
            return packet;
        }

        @Override
        public ByteList getSkyLight(int sectionY) {
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            return section.getSkyLight();
        }

        @Override
        public ByteList getBlockLight(int sectionY) {
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            return section.getBlockLight();
        }

        @Override
        public void setSkyLight(int sectionY, ByteList light) {
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            section.setSkyLight(light);
        }

        @Override
        public void setBlockLight(int sectionY, ByteList light) {
            Section section = getSection(sectionY);
            if (section == null) throw new IllegalStateException("Section " + sectionY + " is not loaded");
            section.setBlockLight(light);
        }

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            int sectionY = y / Section.SIZE_Y;
            Section section = getSection(sectionY - minSection);
            if (section == null) return null;
            return section.getBlock(x, y, z, condition);
        }

        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            int sectionY = y / Section.SIZE_Y;
            Section section = getSection(sectionY - minSection);
            if (section == null) throw new IllegalStateException("Section not loaded");
            section.setBiome(x, y, z, biome);
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            int sectionY = y / Section.SIZE_Y;
            Section section = getSection(sectionY - minSection);
            if (section == null) throw new IllegalStateException("Section not loaded");
            return section.getBiome(x, y, z);
        }
    }
}
