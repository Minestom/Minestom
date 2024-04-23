package net.minestom.server.instance.anvil;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.kyori.adventure.nbt.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AnvilLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilLoader.class);
    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();
    private final static Biome PLAINS = BIOME_MANAGER.getByName(NamespaceID.from("minecraft:plains"));

    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();
    private final Path path;
    private final Path levelPath;
    private final Path regionPath;

    private static class RegionCache extends ConcurrentHashMap<IntIntImmutablePair, Set<IntIntImmutablePair>> {
    }

    /**
     * Represents the chunks currently loaded per region. Used to determine when a region file can be unloaded.
     */
    private final RegionCache perRegionLoadedChunks = new RegionCache();
    private final ReentrantLock perRegionLoadedChunksLock = new ReentrantLock();

    // thread local to avoid contention issues with locks
//    private final ThreadLocal<Int2ObjectMap<BlockState>> blockStateId2ObjectCacheTLS = ThreadLocal.withInitial(Int2ObjectArrayMap::new);

    public AnvilLoader(@NotNull Path path) {
        this.path = path;
        this.levelPath = path.resolve("level.dat");
        this.regionPath = path.resolve("region");
    }

    public AnvilLoader(@NotNull String path) {
        this(Path.of(path));
    }

    @Override
    public void loadInstance(@NotNull Instance instance) {
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
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        if (!Files.exists(path)) {
            // No world folder
            return CompletableFuture.completedFuture(null);
        }
        try {
            return loadMCA(instance, chunkX, chunkZ);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return CompletableFuture.completedFuture(null);
        }
    }

    private @NotNull CompletableFuture<@Nullable Chunk> loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException {
        final RegionFile mcaFile = getMCAFile(chunkX, chunkZ);
        if (mcaFile == null)
            return CompletableFuture.completedFuture(null);
        final CompoundBinaryTag chunkData = mcaFile.readChunkData(chunkX, chunkZ);
        if (chunkData == null)
            return CompletableFuture.completedFuture(null);

        // Ensure the chunk matches the expected Y range
        DimensionType dimensionType = instance.getDimensionType();
        int minY = chunkData.getInt("yPos") * Chunk.CHUNK_SECTION_SIZE;
        Check.stateCondition(minY != dimensionType.getMinY(), "Trying to load chunk with minY = {0}, but instance dimension type ({1}) has a minY of {2}",
                minY, dimensionType.getName().asString(), dimensionType.getMinY());
        int maxY = minY + (chunkData.getList("sections", BinaryTagTypes.COMPOUND).size() * Chunk.CHUNK_SECTION_SIZE);
        Check.stateCondition(maxY != dimensionType.getMaxY(), "Trying to load chunk with maxY = {0}, but instance dimension type ({1}) has a maxY of {2}",
                maxY, dimensionType.getName().asString(), dimensionType.getMaxY());

        // Load the chunk data (assuming it is fully generated)
        final Chunk chunk = instance.getChunkSupplier().createChunk(instance, chunkX, chunkZ);
        synchronized (chunk) { // todo: boo, synchronized
            final String status = chunkData.getString("status");

            // TODO: Should we handle other states?
            if (status.isEmpty() || "minecraft:full".equals(status)) {
                // TODO: Parallelize block, block entities and biome loading
                // Blocks + Biomes
                loadSections(chunk, chunkData);

                // Block entities
                loadBlockEntities(chunk, chunkData);
            } else {
                LOGGER.warn("Skipping partially generated chunk at {}, {} with status {}", chunkX, chunkZ, status);
            }
        }

        // Cache the index of the loaded chunk
        perRegionLoadedChunksLock.lock();
        try {
            int regionX = ChunkUtils.toRegionCoordinate(chunkX);
            int regionZ = ChunkUtils.toRegionCoordinate(chunkZ);
            var chunks = perRegionLoadedChunks.computeIfAbsent(new IntIntImmutablePair(regionX, regionZ), r -> new HashSet<>()); // region cache may have been removed on another thread due to unloadChunk
            chunks.add(new IntIntImmutablePair(chunkX, chunkZ));
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private @Nullable RegionFile getMCAFile(int chunkX, int chunkZ) {
        final int regionX = ChunkUtils.toRegionCoordinate(chunkX);
        final int regionZ = ChunkUtils.toRegionCoordinate(chunkZ);
        return alreadyLoaded.computeIfAbsent(RegionFile.getFileName(regionX, regionZ), n -> {
            final Path regionPath = this.regionPath.resolve(n);
            if (!Files.exists(regionPath)) {
                return null;
            }
            perRegionLoadedChunksLock.lock();
            try {
                Set<IntIntImmutablePair> previousVersion = perRegionLoadedChunks.put(new IntIntImmutablePair(regionX, regionZ), new HashSet<>());
                assert previousVersion == null : "The AnvilLoader cache should not already have data for this region.";
                return new RegionFile(regionPath);
            } catch (IOException e) {
                MinecraftServer.getExceptionManager().handleException(e);
                return null;
            } finally {
                perRegionLoadedChunksLock.unlock();
            }
        });
    }

    private void loadSections(@NotNull Chunk chunk, @NotNull CompoundBinaryTag chunkData) {
        for (BinaryTag sectionTag : chunkData.getList("sections", BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag sectionData = (CompoundBinaryTag) sectionTag;

            final int sectionY = sectionData.getInt("Y", Integer.MIN_VALUE);
            Check.stateCondition(sectionY == Integer.MIN_VALUE, "Missing section Y value");
            final int yOffset = Chunk.CHUNK_SECTION_SIZE * sectionY;

            final Section section = chunk.getSection(sectionY);

            // Lighting
            if (sectionData.get("SkyLight") instanceof ByteArrayBinaryTag skyLightTag && skyLightTag.size() == 2048) {
                section.setSkyLight(skyLightTag.value());
            }
            if (sectionData.get("BlockLight") instanceof ByteArrayBinaryTag blockLightTag && blockLightTag.size() == 2048) {
                section.setBlockLight(blockLightTag.value());
            }

            {   // Biomes
                final CompoundBinaryTag biomesTag = sectionData.getCompound("biomes");
                final ListBinaryTag biomePaletteTag = biomesTag.getList("palette", BinaryTagTypes.STRING);
                Biome[] convertedPalette = loadBiomePalette(biomePaletteTag);

                if (convertedPalette.length == 1) {
                    // One solid block, no need to check the data
                    section.biomePalette().fill(BIOME_MANAGER.getId(convertedPalette[0]));
                } else if (convertedPalette.length > 1) {
                    final long[] packedIndices = biomesTag.getLongArray("data");
                    Check.stateCondition(packedIndices.length == 0, "Missing packed biomes data");
                    int[] biomeIndices = new int[64];
                    ArrayUtils.unpack(biomeIndices, packedIndices, packedIndices.length * 64 / biomeIndices.length);

                    section.biomePalette().setAll((x, y, z) -> {
                        final int index = x + z * 4 + y * 16;
                        final Biome biome = convertedPalette[biomeIndices[index]];
                        return BIOME_MANAGER.getId(biome);
                    });
                }
            }

            {   // Blocks
                final CompoundBinaryTag blockStatesTag = sectionData.getCompound("block_states");
                final ListBinaryTag blockPaletteTag = blockStatesTag.getList("palette", BinaryTagTypes.COMPOUND);
                Block[] convertedPalette = loadBlockPalette(blockPaletteTag);
                if (blockPaletteTag.size() == 1) {
                    // One solid block, no need to check the data
                    section.blockPalette().fill(convertedPalette[0].stateId());
                } else if (blockPaletteTag.size() > 1) {
                    final long[] packedStates = blockStatesTag.getLongArray("data");
                    Check.stateCondition(packedStates.length == 0, "Missing packed states data");
                    int[] blockStateIndices = new int[Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE];
                    ArrayUtils.unpack(blockStateIndices, packedStates, packedStates.length * 64 / blockStateIndices.length);

                    for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                        for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                            for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
                                try {
                                    final int blockIndex = y * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE + z * Chunk.CHUNK_SECTION_SIZE + x;
                                    final int paletteIndex = blockStateIndices[blockIndex];
                                    final Block block = convertedPalette[paletteIndex];

                                    chunk.setBlock(x, y + yOffset, z, block);
                                } catch (Exception e) {
                                    MinecraftServer.getExceptionManager().handleException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Block[] loadBlockPalette(@NotNull ListBinaryTag paletteTag) {
        Block[] convertedPalette = new Block[paletteTag.size()];
        for (int i = 0; i < convertedPalette.length; i++) {
            CompoundBinaryTag paletteEntry = paletteTag.getCompound(i);
            String blockName = paletteEntry.getString("Name");
            if (blockName.equals("minecraft:air")) {
                convertedPalette[i] = Block.AIR;
            } else {
                Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName), "Unknown block " + blockName);
                // Properties
                final Map<String, String> properties = new HashMap<>();
                CompoundBinaryTag propertiesNBT = paletteEntry.getCompound("Properties");
                for (var property : propertiesNBT) {
                    if (property.getValue() instanceof StringBinaryTag propertyValue) {
                        properties.put(property.getKey(), propertyValue.value());
                    } else {
                        LOGGER.warn("Fail to parse block state properties {}, expected a string for {}, but contents were {}",
                                propertiesNBT, property.getKey(), TagStringIOExt.writeTag(property.getValue()));
                    }
                }
                if (!properties.isEmpty()) block = block.withProperties(properties);

                // Handler
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(block.name());
                if (handler != null) block = block.withHandler(handler);

                convertedPalette[i] = block;
            }
        }
        return convertedPalette;
    }

    private Biome[] loadBiomePalette(@NotNull ListBinaryTag paletteTag) {
        Biome[] convertedPalette = new Biome[paletteTag.size()];
        for (int i = 0; i < convertedPalette.length; i++) {
            final String name = paletteTag.getString(i);
            convertedPalette[i] = Objects.requireNonNullElse(BIOME_MANAGER.getByName(name), PLAINS);
        }
        return convertedPalette;
    }

    private void loadBlockEntities(@NotNull Chunk loadedChunk, @NotNull CompoundBinaryTag chunkData) {
        for (BinaryTag blockEntityTag : chunkData.getList("block_entities", BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag blockEntity = (CompoundBinaryTag) blockEntityTag;

            final int x = blockEntity.getInt("x");
            final int y = blockEntity.getInt("y");
            final int z = blockEntity.getInt("z");
            Block block = loadedChunk.getBlock(x, y, z);

            // Load the block handler if the id is present
            if (blockEntity.get("id") instanceof StringBinaryTag blockEntityId) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandlerOrDummy(blockEntityId.value());
                block = block.withHandler(handler);
            }

            // Remove anvil tags
            CompoundBinaryTag trimmedTag = CompoundBinaryTag.builder().put(blockEntity)
                    .remove("id").remove("keepPacked")
                    .remove("x").remove("y").remove("z")
                    .build();

            // Place block
            final var finalBlock = trimmedTag.size() > 0 ? block.withNbt(trimmedTag) : block;
            loadedChunk.setBlock(x, y, z, finalBlock);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance(@NotNull Instance instance) {
        final CompoundBinaryTag nbt = instance.tagHandler().asCompound();
        if (nbt.size() == 0) {
            // Instance has no data
            return AsyncUtils.VOID_FUTURE;
        }
        try (OutputStream os = Files.newOutputStream(levelPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            BinaryTagIO.writer().writeNamed(Map.entry("", nbt), os, BinaryTagIO.Compression.GZIP);
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        return AsyncUtils.VOID_FUTURE;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        // Find the region file or create an empty one if missing
        RegionFile mcaFile = getMCAFile(chunkX, chunkZ);
        if (mcaFile == null) {
            final int regionX = ChunkUtils.toRegionCoordinate(chunkX);
            final int regionZ = ChunkUtils.toRegionCoordinate(chunkZ);
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
                LOGGER.error("Failed to create region file for " + chunkX + ", " + chunkZ, e);
                MinecraftServer.getExceptionManager().handleException(e);
                return AsyncUtils.VOID_FUTURE;
            }
        }

        try {
            final CompoundBinaryTag.Builder chunkData = CompoundBinaryTag.builder();

            chunkData.putInt("DataVersion", MinecraftServer.DATA_VERSION);
            chunkData.putInt("xPos", chunkX);
            chunkData.putInt("zPos", chunkZ);
            chunkData.putInt("yPos", chunk.getMinSection());
            chunkData.putString("status", "minecraft:full");
            chunkData.putLong("LastUpdate", chunk.getInstance().getWorldAge());

            saveSectionData(chunk, chunkData);

            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeChunkData(chunkX, chunkZ, chunkData.build());
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
        }
        return AsyncUtils.VOID_FUTURE;
    }

//    private BlockState getBlockState(final Block block) {
//        return blockStateId2ObjectCacheTLS.get().computeIfAbsent(block.stateId(), _unused -> new BlockState(block.name(), block.properties()));
//    }

    private void saveSectionData(@NotNull Chunk chunk, @NotNull CompoundBinaryTag.Builder chunkData) {


        final int minY = chunk.getMinSection() * Chunk.CHUNK_SECTION_SIZE;
        final int maxY = chunk.getMaxSection() * Chunk.CHUNK_SECTION_SIZE - 1;
        for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
            final Section section = chunk.getSection(sectionY);

            final CompoundBinaryTag.Builder sectionData = CompoundBinaryTag.builder();
            sectionData.putInt("Y", sectionY);

            // Lighting
            byte[] skyLight = section.skyLight().array();
            if (skyLight != null && skyLight.length > 0)
                sectionData.putByteArray("SkyLight", skyLight);
            byte[] blockLight = section.blockLight().array();
            if (blockLight != null && blockLight.length > 0)
                sectionData.putByteArray("BlockLight", blockLight);

            // Build block & biome palettes
            //todo
//            int[] blockStates = new int[Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE];
//            int[] biomes = new int[64];
//
//            for (int localY = 0; localY < Chunk.CHUNK_SECTION_SIZE; localY++) {
//                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
//                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
//
//                    }
//                }
//            }
            throw new UnsupportedOperationException("Not implemented");
        }

    }
//    private void save(Chunk chunk, ChunkWriter chunkWriter) {
//        final int minY = chunk.getMinSection() * Chunk.CHUNK_SECTION_SIZE;
//        final int maxY = chunk.getMaxSection() * Chunk.CHUNK_SECTION_SIZE - 1;
//        chunkWriter.setYPos(minY);
//        List<NBTCompound> blockEntities = new ArrayList<>();
//        chunkWriter.setStatus(ChunkColumn.GenerationStatus.Full);
//
//        List<NBTCompound> sectionData = new ArrayList<>((maxY - minY + 1) / Chunk.CHUNK_SECTION_SIZE);
//        int[] palettedBiomes = new int[ChunkSection.Companion.getBiomeArraySize()];
//        int[] palettedBlockStates = new int[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
//        for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {

//            for (int sectionLocalY = 0; sectionLocalY < Chunk.CHUNK_SECTION_SIZE; sectionLocalY++) {
//                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
//                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
//                        final int y = sectionLocalY + sectionY * Chunk.CHUNK_SECTION_SIZE;
//
//                        final int blockIndex = x + sectionLocalY * 16 * 16 + z * 16;
//
//                        final Block block = chunk.getBlock(x, y, z);
//
//                        final BlockState hephaistosBlockState = getBlockState(block);
//                        blockPalette.increaseReference(hephaistosBlockState);
//
//                        palettedBlockStates[blockIndex] = blockPalette.getPaletteIndex(hephaistosBlockState);
//
//                        // biome are stored for 4x4x4 volumes, avoid unnecessary work
//                        if (x % 4 == 0 && sectionLocalY % 4 == 0 && z % 4 == 0) {
//                            int biomeIndex = (x / 4) + (sectionLocalY / 4) * 4 * 4 + (z / 4) * 4;
//                            final Biome biome = chunk.getBiome(x, y, z);
//                            final String biomeName = biome.name();
//
//                            biomePalette.increaseReference(biomeName);
//                            palettedBiomes[biomeIndex] = biomePalette.getPaletteIndex(biomeName);
//                        }
//
//                        // Block entities
//                        final BlockHandler handler = block.handler();
//                        final NBTCompound originalNBT = block.nbt();
//                        if (originalNBT != null || handler != null) {
//                            MutableNBTCompound nbt = originalNBT != null ?
//                                    originalNBT.toMutableCompound() : new MutableNBTCompound();
//
//                            if (handler != null) {
//                                nbt.setString("id", handler.getNamespaceId().asString());
//                            }
//                            nbt.setInt("x", x + Chunk.CHUNK_SIZE_X * chunk.getChunkX());
//                            nbt.setInt("y", y);
//                            nbt.setInt("z", z + Chunk.CHUNK_SIZE_Z * chunk.getChunkZ());
//                            nbt.setByte("keepPacked", (byte) 0);
//                            blockEntities.add(nbt.toCompound());
//                        }
//                    }
//                }
//            }
//
//            sectionWriter.setPalettedBiomes(biomePalette, palettedBiomes);
//            sectionWriter.setPalettedBlockStates(blockPalette, palettedBlockStates);
//
//            sectionData.add(sectionWriter.toNBT());
//        }
//
//        chunkWriter.setSectionsData(NBT.List(NBTType.TAG_Compound, sectionData));
//        chunkWriter.setBlockEntityData(NBT.List(NBTType.TAG_Compound, blockEntities));
//    }

    /**
     * Unload a given chunk. Also unloads a region when no chunk from that region is loaded.
     *
     * @param chunk the chunk to unload
     */
    @Override
    public void unloadChunk(Chunk chunk) {
        final int regionX = ChunkUtils.toRegionCoordinate(chunk.getChunkX());
        final int regionZ = ChunkUtils.toRegionCoordinate(chunk.getChunkZ());
        final IntIntImmutablePair regionKey = new IntIntImmutablePair(regionX, regionZ);

        perRegionLoadedChunksLock.lock();
        try {
            Set<IntIntImmutablePair> chunks = perRegionLoadedChunks.get(regionKey);
            if (chunks != null) { // if null, trying to unload a chunk from a region that was not created by the AnvilLoader
                // don't check return value, trying to unload a chunk not created by the AnvilLoader is valid
                chunks.remove(new IntIntImmutablePair(chunk.getChunkX(), chunk.getChunkZ()));

                if (chunks.isEmpty()) {
                    perRegionLoadedChunks.remove(regionKey);
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
