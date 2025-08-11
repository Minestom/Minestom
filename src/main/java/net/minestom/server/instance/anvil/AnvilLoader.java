package net.minestom.server.instance.anvil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.nbt.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palettes;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.coordinate.CoordConversion.*;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

public class AnvilLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilLoader.class);
    private static final DynamicRegistry<Biome> BIOME_REGISTRY = MinecraftServer.getBiomeRegistry();
    private final static int PLAINS_ID = BIOME_REGISTRY.getId(Biome.PLAINS);
    private static final CompoundBinaryTag[] BLOCK_STATE_ID_2_OBJECT_CACHE = new CompoundBinaryTag[Block.statesCount()];

    private final ReentrantLock fileCreationLock = new ReentrantLock();
    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();
    private final Path path;
    private final Path levelPath;
    private final Path regionPath;

    /**
     * Represents the chunks currently loaded per region. Used to determine when a region file can be unloaded.
     * <p>
     * RegionIndex = Set<ChunkIndex>
     */
    private final Long2ObjectOpenHashMap<LongSet> perRegionLoadedChunks = new Long2ObjectOpenHashMap<>();
    private final ReentrantLock perRegionLoadedChunksLock = new ReentrantLock();

    public AnvilLoader(Path path) {
        this.path = path;
        this.levelPath = path.resolve("level.dat");
        this.regionPath = path.resolve("region");
    }

    public AnvilLoader(String path) {
        this(Path.of(path));
    }

    @Override
    public void loadInstance(Instance instance) {
        if (!Files.exists(levelPath)) {
            return;
        }
        try (InputStream is = Files.newInputStream(levelPath)) {
            final CompoundBinaryTag tag = BinaryTagIO.reader().readNamed(is, BinaryTagIO.Compression.GZIP).getValue();
            Files.copy(levelPath, path.resolve("level.dat_old"), StandardCopyOption.REPLACE_EXISTING);
            instance.tagHandler().updateContent(tag);
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    @Override
    public @Nullable Chunk loadChunk(Instance instance, int chunkX, int chunkZ) {
        if (!Files.exists(path)) {
            // No world folder
            return null;
        }
        try {
            return loadMCA(instance, chunkX, chunkZ);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    private @Nullable Chunk loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException {
        final RegionFile mcaFile = getMCAFile(chunkX, chunkZ);
        if (mcaFile == null) return null;
        final CompoundBinaryTag chunkData = mcaFile.readChunkData(chunkX, chunkZ);
        if (chunkData == null) return null;

        // Load the chunk data (assuming it is fully generated)
        final Chunk chunk = instance.getChunkSupplier().createChunk(instance, chunkX, chunkZ);
        synchronized (chunk) { // todo: boo, synchronized
            final String status = chunkData.getString("status");
            // TODO: Should we handle other statuses?
            if (status.isEmpty() || "minecraft:full".equals(status)) {
                // Blocks + Biomes
                loadSections(chunk, chunkData);
                // Block entities
                loadBlockEntities(chunk, chunkData);
                chunk.loadHeightmapsFromNBT(chunkData.getCompound("Heightmaps"));
            } else {
                LOGGER.warn("Skipping partially generated chunk at {}, {} with status {}", chunkX, chunkZ, status);
            }
            CompoundBinaryTag handlerData = CompoundBinaryTag.builder()
                    .put(chunkData)
                    .remove("Heightmaps")
                    .remove("sections")
                    .remove("sections")
                    .remove("block_entities")
                    .build();
            chunk.tagHandler().updateContent(handlerData);
        }

        // Cache the index of the loaded chunk
        perRegionLoadedChunksLock.lock();
        try {
            final int regionX = chunkToRegion(chunkX), regionZ = chunkToRegion(chunkZ);
            final long regionIndex = regionIndex(regionX, regionZ);
            var chunks = perRegionLoadedChunks.computeIfAbsent(regionIndex, r -> new LongOpenHashSet()); // region cache may have been removed on another thread due to unloadChunk
            final long chunkIndex = chunkIndex(chunkX, chunkZ);
            chunks.add(chunkIndex);
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
        return chunk;
    }

    private @Nullable RegionFile getMCAFile(int chunkX, int chunkZ) {
        final int regionX = chunkToRegion(chunkX), regionZ = chunkToRegion(chunkZ);
        final String fileName = RegionFile.getFileName(regionX, regionZ);

        final RegionFile loadedFile = alreadyLoaded.get(fileName);
        if (loadedFile != null) return loadedFile;

        perRegionLoadedChunksLock.lock();
        try {
            return alreadyLoaded.computeIfAbsent(fileName, n -> {
                final Path regionPath = this.regionPath.resolve(n);
                if (!Files.exists(regionPath)) {
                    return null;
                }

                try {
                    final long regionIndex = regionIndex(regionX, regionZ);
                    LongSet previousVersion = perRegionLoadedChunks.put(regionIndex, new LongOpenHashSet());
                    assert previousVersion == null : "The AnvilLoader cache should not already have data for this region.";
                    return new RegionFile(regionPath);
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    return null;
                }
            });
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
    }

    private void loadSections(Chunk chunk, CompoundBinaryTag chunkData) {
        for (BinaryTag sectionTag : chunkData.getList("sections", BinaryTagTypes.COMPOUND)) {
            if (!(sectionTag instanceof CompoundBinaryTag sectionData)) {
                LOGGER.warn("Invalid section tag in chunk data: {}", sectionTag);
                continue;
            }

            final int sectionY = sectionData.getInt("Y", Integer.MIN_VALUE);
            Check.stateCondition(sectionY == Integer.MIN_VALUE, "Missing section Y value");
            if (sectionY < chunk.getMinSection() || sectionY >= chunk.getMaxSection()) {
                // Vanilla stores a section below and above the world for lighting, throw it out.
                continue;
            }

            final Section section = chunk.getSection(sectionY);

            // Lighting
            if (sectionData.get("SkyLight") instanceof ByteArrayBinaryTag skyLightTag && skyLightTag.size() == 2048) {
                section.skyLight().set(skyLightTag.value());
            }
            if (sectionData.get("BlockLight") instanceof ByteArrayBinaryTag blockLightTag && blockLightTag.size() == 2048) {
                section.blockLight().set(blockLightTag.value());
            }

            {   // Biomes
                final CompoundBinaryTag biomesTag = sectionData.getCompound("biomes");
                final ListBinaryTag biomePaletteTag = biomesTag.getList("palette", BinaryTagTypes.STRING);
                int[] convertedBiomePalette = loadBiomePalette(biomePaletteTag);
                if (convertedBiomePalette.length == 1) {
                    // One solid block, no need to check the data
                    section.biomePalette().fill(convertedBiomePalette[0]);
                } else if (convertedBiomePalette.length > 1) {
                    final long[] packedIndices = biomesTag.getLongArray("data");
                    Check.stateCondition(packedIndices.length == 0, "Missing packed biomes data");
                    section.biomePalette().load(convertedBiomePalette, packedIndices);
                }
            }

            {   // Blocks
                final CompoundBinaryTag blockStatesTag = sectionData.getCompound("block_states");
                final ListBinaryTag blockPaletteTag = blockStatesTag.getList("palette", BinaryTagTypes.COMPOUND);
                final int[] convertedPalette = loadBlockPalette(blockPaletteTag);
                if (blockPaletteTag.size() == 1) {
                    // One solid block, no need to check the data
                    section.blockPalette().fill(convertedPalette[0]);
                } else if (blockPaletteTag.size() > 1) {
                    final long[] packedStates = blockStatesTag.getLongArray("data");
                    Check.stateCondition(packedStates.length == 0, "Missing packed states data");
                    section.blockPalette().load(convertedPalette, packedStates);
                }
            }
        }
    }

    private int[] loadBlockPalette(ListBinaryTag paletteTag) {
        final int length = paletteTag.size();
        int[] convertedPalette = new int[length];
        for (int i = 0; i < length; i++) {
            CompoundBinaryTag paletteEntry = paletteTag.getCompound(i);
            final String blockName = paletteEntry.getString("Name");
            if (blockName.equals("minecraft:air")) {
                convertedPalette[i] = Block.AIR.stateId();
            } else {
                Block block = Objects.requireNonNull(Block.fromKey(blockName), "Unknown block " + blockName);
                // Properties
                final CompoundBinaryTag propertiesNBT = paletteEntry.getCompound("Properties");
                if (!propertiesNBT.isEmpty()) {
                    final Map<String, String> properties = HashMap.newHashMap(propertiesNBT.size());
                    for (var property : propertiesNBT) {
                        if (property.getValue() instanceof StringBinaryTag propertyValue) {
                            properties.put(property.getKey(), propertyValue.value());
                        } else {
                            try {
                                LOGGER.warn("Fail to parse block state properties {}, expected a string tag for {}, but contents were {}",
                                        propertiesNBT, property.getKey(), MinestomAdventure.tagStringIO().asString(property.getValue()));
                            } catch (IOException e) {
                                LOGGER.warn("Fail to parse block state properties {}, expected a string tag for {}, but contents were a {} tag", propertiesNBT, property.getKey(), property.getValue().examinableName());
                            }
                        }
                    }
                    block = block.withProperties(properties);
                }

                convertedPalette[i] = block.stateId();
            }
        }
        return convertedPalette;
    }

    private int[] loadBiomePalette(ListBinaryTag paletteTag) {
        final int length = paletteTag.size();
        int[] convertedPalette = new int[length];
        for (int i = 0; i < length; i++) {
            final String name = paletteTag.getString(i);
            int biomeId = BIOME_REGISTRY.getId(RegistryKey.unsafeOf(name));
            if (biomeId == -1) biomeId = PLAINS_ID;
            convertedPalette[i] = biomeId;
        }
        return convertedPalette;
    }

    private void loadBlockEntities(Chunk loadedChunk, CompoundBinaryTag chunkData) {
        for (BinaryTag blockEntityTag : chunkData.getList("block_entities", BinaryTagTypes.COMPOUND)) {
            if (!(blockEntityTag instanceof CompoundBinaryTag blockEntity)) {
                LOGGER.warn("Invalid block entity tag in chunk data: {}", blockEntityTag);
                continue;
            }
            final int x = blockEntity.getInt("x"), y = blockEntity.getInt("y"), z = blockEntity.getInt("z");
            final int localX = globalToSectionRelative(x), localY = globalToSectionRelative(y), localZ = globalToSectionRelative(z);
            Section section = loadedChunk.getSectionAt(y);
            final int stateId = section.blockPalette().get(localX, localY, localZ);
            Block block = Block.fromStateId(stateId);
            assert block != null;
            // Load the block handler if the id is present
            if (blockEntity.get("id") instanceof StringBinaryTag blockEntityId) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandlerOrDummy(blockEntityId.value());
                block = block.withHandler(handler);
            }
            // Remove anvil tags
            CompoundBinaryTag trimmedTag = CompoundBinaryTag.builder()
                    .put(blockEntity)
                    .remove("id").remove("keepPacked")
                    .remove("x").remove("y").remove("z")
                    .build();

            // Place block
            final Block finalBlock = !trimmedTag.isEmpty() ? block.withNbt(trimmedTag) : block;
            section.cacheBlock(localX, localY, localZ, finalBlock);
        }
    }

    @Override
    public void saveInstance(Instance instance) {
        final CompoundBinaryTag nbt = instance.tagHandler().asCompound();
        if (nbt.isEmpty()) {
            // Instance has no data
            return;
        }
        try (OutputStream os = Files.newOutputStream(levelPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            BinaryTagIO.writer().writeNamed(Map.entry("", nbt), os, BinaryTagIO.Compression.GZIP);
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    @Override
    public void saveChunk(Chunk chunk) {
        final int chunkX = chunk.getChunkX(), chunkZ = chunk.getChunkZ();
        final int regionX = chunkToRegion(chunkX), regionZ = chunkToRegion(chunkZ);
        final long chunkIndex = chunkIndex(chunkX, chunkZ);
        final long regionIndex = regionIndex(regionX, regionZ);

        // Find the region file or create an empty one if missing
        RegionFile mcaFile;
        fileCreationLock.lock();
        try {
            mcaFile = getMCAFile(chunkX, chunkZ);

            if (mcaFile == null) {
                final String regionFileName = RegionFile.getFileName(regionX, regionZ);
                try {
                    Path regionFile = regionPath.resolve(regionFileName);
                    if (!Files.exists(regionFile)) {
                        Files.createDirectories(regionFile.getParent());
                        Files.createFile(regionFile);
                    }

                    mcaFile = new RegionFile(regionFile);
                    alreadyLoaded.put(regionFileName, mcaFile);
                } catch (IOException e) {
                    LOGGER.error("Failed to create region file for {}, {}", chunkX, chunkZ, e);
                    MinecraftServer.getExceptionManager().handleException(e);
                    return;
                }
            }
        } finally {
            fileCreationLock.unlock();

            this.perRegionLoadedChunksLock.lock();
            try {
                this.perRegionLoadedChunks.computeIfAbsent(regionIndex, k -> new LongOpenHashSet())
                        .add(chunkIndex);
            } finally {
                this.perRegionLoadedChunksLock.unlock();
            }
        }

        try {
            final CompoundBinaryTag.Builder chunkData = CompoundBinaryTag.builder();

            chunkData.put(chunk.tagHandler().asCompound());

            chunkData.putInt("DataVersion", MinecraftServer.DATA_VERSION);
            chunkData.putInt("xPos", chunkX);
            chunkData.putInt("zPos", chunkZ);
            chunkData.putInt("yPos", chunk.getMinSection());
            chunkData.putString("status", "minecraft:full");
            chunkData.putLong("LastUpdate", chunk.getInstance().getWorldAge());

            saveSectionData(chunk, chunkData);

            mcaFile.writeChunkData(chunkX, chunkZ, chunkData.build());
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk {}, {}", chunkX, chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    private void saveSectionData(Chunk chunk, CompoundBinaryTag.Builder chunkData) {
        final ListBinaryTag.Builder<CompoundBinaryTag> sections = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        final ListBinaryTag.Builder<CompoundBinaryTag> blockEntities = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);

        // Block & Biome arrays reused for each chunk
        List<BinaryTag> biomePalette = new ArrayList<>();
        int[] biomeIndices = new int[64];

        List<BinaryTag> blockPaletteEntries = new ArrayList<>();
        IntList blockPaletteIndices = new IntArrayList(); // Map block indices by state id to avoid doing a deep comparison on every block tag
        int[] blockIndices = new int[SECTION_BLOCK_COUNT];

        synchronized (chunk) {
            for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                final Section section = chunk.getSection(sectionY);

                final CompoundBinaryTag.Builder sectionData = CompoundBinaryTag.builder();
                sectionData.putByte("Y", (byte) sectionY);

                // Lighting
                byte[] skyLight = section.skyLight().array();
                if (skyLight != null && skyLight.length > 0) sectionData.putByteArray("SkyLight", skyLight);
                byte[] blockLight = section.blockLight().array();
                if (blockLight != null && blockLight.length > 0) sectionData.putByteArray("BlockLight", blockLight);

                final int globalSectionY = sectionY * 16;
                // Retrieve block data
                if (section.blockPalette().singleValue() != -1) {
                    blockPaletteIndices.add(section.blockPalette().singleValue());
                } else {
                    section.blockPalette().getAll((x, y, z, value) -> {
                        Block block = section.entries().get(sectionBlockIndex(x, y, z));
                        if (block == null) block = Block.fromStateId(value);
                        assert block != null;
                        final CompoundBinaryTag blockState = blockStateNbt(block);
                        int blockPaletteIndex = blockPaletteIndices.indexOf(value);
                        if (blockPaletteIndex == -1) {
                            blockPaletteIndex = blockPaletteEntries.size();
                            blockPaletteEntries.add(blockState);
                            blockPaletteIndices.add(value);
                        }
                        final int blockIndex = x + y * 16 * 16 + z * 16;
                        blockIndices[blockIndex] = blockPaletteIndex;

                        // Add block entity if present
                        final BlockHandler handler = block.handler();
                        final CompoundBinaryTag originalNBT = block.nbt();
                        if (originalNBT != null || handler != null) {
                            CompoundBinaryTag.Builder blockEntityTag = CompoundBinaryTag.builder();
                            if (originalNBT != null) blockEntityTag.put(originalNBT);
                            if (handler != null) blockEntityTag.putString("id", handler.getKey().asString());
                            blockEntityTag.putInt("x", x + CHUNK_SIZE_X * chunk.getChunkX());
                            blockEntityTag.putInt("y", y + globalSectionY);
                            blockEntityTag.putInt("z", z + CHUNK_SIZE_Z * chunk.getChunkZ());
                            blockEntityTag.putByte("keepPacked", (byte) 0);
                            blockEntities.add(blockEntityTag.build());
                        }
                    });
                }
                // Retrieve biome data
                if (section.biomePalette().singleValue() != -1) {
                    blockPaletteIndices.add(section.biomePalette().singleValue());
                } else {
                    section.biomePalette().getAll((x, y, z, value) -> {
                        int biomeIndex = (x / 4) + (y / 4) * 4 * 4 + (z / 4) * 4;
                        final RegistryKey<Biome> biomeKey = MinecraftServer.getBiomeRegistry().getKey(value);
                        assert biomeKey != null;
                        final BinaryTag biomeName = StringBinaryTag.stringBinaryTag(biomeKey.key().asString());
                        int biomePaletteIndex = biomePalette.indexOf(biomeName);
                        if (biomePaletteIndex == -1) {
                            biomePaletteIndex = biomePalette.size();
                            biomePalette.add(biomeName);
                        }
                        biomeIndices[biomeIndex] = biomePaletteIndex;
                    });
                }

                // Save the block and biome palettes
                final CompoundBinaryTag.Builder blockStates = CompoundBinaryTag.builder();
                blockStates.put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, blockPaletteEntries));
                if (blockPaletteEntries.size() > 1) {
                    // If there is only one entry we do not need to write the packed indices
                    final int bitsPerEntry = (int) Math.max(4, Math.ceil(Math.log(blockPaletteEntries.size()) / Math.log(2)));
                    blockStates.putLongArray("data", Palettes.pack(blockIndices, bitsPerEntry));
                }
                sectionData.put("block_states", blockStates.build());

                final CompoundBinaryTag.Builder biomes = CompoundBinaryTag.builder();
                biomes.put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING, biomePalette));
                if (biomePalette.size() > 1) {
                    // If there is only one entry we do not need to write the packed indices
                    final int bitsPerEntry = (int) Math.max(1, Math.ceil(Math.log(biomePalette.size()) / Math.log(2)));
                    biomes.putLongArray("data", Palettes.pack(biomeIndices, bitsPerEntry));
                }
                sectionData.put("biomes", biomes.build());

                biomePalette.clear();
                blockPaletteEntries.clear();
                blockPaletteIndices.clear();

                sections.add(sectionData.build());
            }
        }

        chunkData.put("sections", sections.build());
        chunkData.put("block_entities", blockEntities.build());
    }

    private static CompoundBinaryTag blockStateNbt(final Block block) {
        final int stateId = block.stateId();
        CompoundBinaryTag result = BLOCK_STATE_ID_2_OBJECT_CACHE[stateId];
        if (result == null) result = BLOCK_STATE_ID_2_OBJECT_CACHE[stateId] = blockStateNbtCompute(block);
        return result;
    }

    private static CompoundBinaryTag blockStateNbtCompute(final Block block) {
        final CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
        tag.putString("Name", block.name());
        if (!block.properties().isEmpty()) {
            final Map<String, String> defaultProperties = block.defaultState().properties();
            final CompoundBinaryTag.Builder propertiesTag = CompoundBinaryTag.builder();
            for (Map.Entry<String, String> entry : block.properties().entrySet()) {
                final String key = entry.getKey(), value = entry.getValue();
                if (defaultProperties.get(key).equals(value))
                    continue; // Skip default values
                propertiesTag.putString(key, value);
            }
            CompoundBinaryTag properties = propertiesTag.build();
            if (!properties.isEmpty()) tag.put("Properties", properties);
        }
        return tag.build();
    }

    /**
     * Unload a given chunk. Also unloads a region when no chunk from that region is loaded.
     *
     * @param chunk the chunk to unload
     */
    @Override
    public void unloadChunk(Chunk chunk) {
        final int regionX = chunkToRegion(chunk.getChunkX()), regionZ = chunkToRegion(chunk.getChunkZ());
        final long regionIndex = regionIndex(regionX, regionZ);

        perRegionLoadedChunksLock.lock();
        try {
            LongSet chunks = perRegionLoadedChunks.get(regionIndex);
            if (chunks != null) { // if null, trying to unload a chunk from a region that was not created by the AnvilLoader
                // don't check return value, trying to unload a chunk not created by the AnvilLoader is valid
                final long chunkIndex = chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
                chunks.remove(chunkIndex);

                if (chunks.isEmpty()) {
                    perRegionLoadedChunks.remove(regionIndex);
                    RegionFile regionFile = alreadyLoaded.remove(RegionFile.getFileName(regionX, regionZ));
                    if (regionFile != null) {
                        try {
                            regionFile.close();
                        } catch (IOException e) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        }
                    }
                }
            }
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
    }

    @Override
    public boolean supportsParallelLoading() {
        return true;
    }

    @Override
    public boolean supportsParallelSaving() {
        return true;
    }
}
