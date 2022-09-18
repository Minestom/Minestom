package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mca.*;
import org.jglrxavpok.hephaistos.mca.readers.ChunkReader;
import org.jglrxavpok.hephaistos.mca.readers.ChunkSectionReader;
import org.jglrxavpok.hephaistos.mca.readers.SectionBiomeInformation;
import org.jglrxavpok.hephaistos.mca.writer.ChunkSectionWriter;
import org.jglrxavpok.hephaistos.mca.writer.ChunkWriter;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AnvilLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilLoader.class);
    private static final Biome BIOME = Biome.PLAINS;

    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();
    private final Path path;
    private final Path levelPath;
    private final Path regionPath;

    private static class RegionCache extends ConcurrentHashMap<IntIntImmutablePair, Set<IntIntImmutablePair>> {}

    /**
     * Represents the chunks currently loaded per region. Used to determine when a region file can be unloaded.
     */
    private final RegionCache perRegionLoadedChunks = new RegionCache();

    // thread local to avoid contention issues with locks
    private final ThreadLocal<Int2ObjectMap<BlockState>> blockStateId2ObjectCacheTLS = new ThreadLocal<>() {
        @Override
        protected Int2ObjectMap<BlockState> initialValue() {
            return new Int2ObjectArrayMap<>();
        }
    };

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
        try (var reader = new NBTReader(Files.newInputStream(levelPath))) {
            final NBTCompound tag = (NBTCompound) reader.read();
            Files.copy(levelPath, path.resolve("level.dat_old"), StandardCopyOption.REPLACE_EXISTING);
            instance.tagHandler().updateContent(tag);
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ);
        if (!Files.exists(path)) {
            // No world folder
            return CompletableFuture.completedFuture(null);
        }
        try {
            return loadMCA(instance, chunkX, chunkZ);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private @NotNull CompletableFuture<@Nullable Chunk> loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException, AnvilException {
        final RegionFile mcaFile = getMCAFile(instance, chunkX, chunkZ);
        if (mcaFile == null)
            return CompletableFuture.completedFuture(null);
        final NBTCompound chunkData = mcaFile.getChunkData(chunkX, chunkZ);
        if (chunkData == null)
            return CompletableFuture.completedFuture(null);

        final ChunkReader chunkReader = new ChunkReader(chunkData);

        Chunk chunk = new DynamicChunk(instance, chunkX, chunkZ);
        synchronized (chunk) {
            var yRange = chunkReader.getYRange();
            if(yRange.getStart() < instance.getDimensionType().getMinY()) {
                throw new AnvilException(
                        String.format("Trying to load chunk with minY = %d, but instance dimension type (%s) has a minY of %d",
                                yRange.getStart(),
                                instance.getDimensionType().getName().asString(),
                                instance.getDimensionType().getMinY()
                        ));
            }
            if(yRange.getEndInclusive() > instance.getDimensionType().getMaxY()) {
                throw new AnvilException(
                        String.format("Trying to load chunk with maxY = %d, but instance dimension type (%s) has a maxY of %d",
                                yRange.getEndInclusive(),
                                instance.getDimensionType().getName().asString(),
                                instance.getDimensionType().getMaxY()
                        ));
            }

            // TODO: Parallelize block, block entities and biome loading
            // Blocks + Biomes
            loadSections(chunk, chunkReader);

            // Block entities
            loadBlockEntities(chunk, chunkReader);
        }
        synchronized (perRegionLoadedChunks) {
            int regionX = CoordinatesKt.chunkToRegion(chunkX);
            int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
            var chunks = perRegionLoadedChunks.computeIfAbsent(new IntIntImmutablePair(regionX, regionZ), r -> new HashSet<>()); // region cache may have been removed on another thread due to unloadChunk
            chunks.add(new IntIntImmutablePair(chunkX, chunkZ));
        };
        return CompletableFuture.completedFuture(chunk);
    }

    private @Nullable RegionFile getMCAFile(Instance instance, int chunkX, int chunkZ) {
        final int regionX = CoordinatesKt.chunkToRegion(chunkX);
        final int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
        return alreadyLoaded.computeIfAbsent(RegionFile.Companion.createFileName(regionX, regionZ), n -> {
            try {
                final Path regionPath = this.regionPath.resolve(n);
                if (!Files.exists(regionPath)) {
                    return null;
                }
                synchronized (perRegionLoadedChunks) {
                    Set<IntIntImmutablePair> previousVersion = perRegionLoadedChunks.put(new IntIntImmutablePair(regionX, regionZ), new HashSet<>());
                    assert previousVersion == null : "The AnvilLoader cache should not already have data for this region.";
                };
                return new RegionFile(new RandomAccessFile(regionPath.toFile(), "rw"), regionX, regionZ, instance.getDimensionType().getMinY(), instance.getDimensionType().getMaxY()-1);
            } catch (IOException | AnvilException e) {
                MinecraftServer.getExceptionManager().handleException(e);
                return null;
            }
        });
    }

    private void loadSections(Chunk chunk, ChunkReader chunkReader) {
        final HashMap<String, Biome> biomeCache = new HashMap<>();
        for (var sectionNBT : chunkReader.getSections()) {
            ChunkSectionReader sectionReader = new ChunkSectionReader(chunkReader.getMinecraftVersion(), sectionNBT);

            if (sectionReader.isSectionEmpty()) continue;
            final int sectionY = sectionReader.getY();
            final int yOffset = Chunk.CHUNK_SECTION_SIZE * sectionY;

            Section section = chunk.getSection(sectionY);

            if(sectionReader.getSkyLight() != null) {
                section.setSkyLight(sectionReader.getSkyLight().copyArray());
            }
            if(sectionReader.getBlockLight() != null) {
                section.setBlockLight(sectionReader.getBlockLight().copyArray());
            }

            // Biomes
            if(chunkReader.getGenerationStatus().compareTo(ChunkColumn.GenerationStatus.Biomes) > 0) {
                SectionBiomeInformation sectionBiomeInformation = chunkReader.readSectionBiomes(sectionReader);

                if(sectionBiomeInformation != null && sectionBiomeInformation.hasBiomeInformation()) {
                    if(sectionBiomeInformation.isFilledWithSingleBiome()) {
                        for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                                    int finalX = chunk.chunkX * Chunk.CHUNK_SIZE_X + x;
                                    int finalZ = chunk.chunkZ * Chunk.CHUNK_SIZE_Z + z;
                                    int finalY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
                                    String biomeName = sectionBiomeInformation.getBaseBiome();
                                    Biome biome = biomeCache.computeIfAbsent(biomeName, n ->
                                            Objects.requireNonNullElse(MinecraftServer.getBiomeManager().getByName(NamespaceID.from(n)), BIOME));
                                    chunk.setBiome(finalX, finalY, finalZ, biome);
                                }
                            }
                        }
                    } else {
                        for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                                    int finalX = chunk.chunkX * Chunk.CHUNK_SIZE_X + x;
                                    int finalZ = chunk.chunkZ * Chunk.CHUNK_SIZE_Z + z;
                                    int finalY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;

                                    int index = x/4 + (z/4) * 4 + (y/4) * 16;
                                    String biomeName = sectionBiomeInformation.getBiomes()[index];
                                    Biome biome = biomeCache.computeIfAbsent(biomeName, n ->
                                            Objects.requireNonNullElse(MinecraftServer.getBiomeManager().getByName(NamespaceID.from(n)), BIOME));
                                    chunk.setBiome(finalX, finalY, finalZ, biome);
                                }
                            }
                        }
                    }
                }
            }

            // Blocks
            final NBTList<NBTCompound> blockPalette = sectionReader.getBlockPalette();
            if(blockPalette != null) {
                int[] blockStateIndices = sectionReader.getUncompressedBlockStateIDs();
                Block[] convertedPalette = new Block[blockPalette.getSize()];
                for (int i = 0; i < convertedPalette.length; i++) {
                    final NBTCompound paletteEntry = blockPalette.get(i);
                    String blockName = Objects.requireNonNull(paletteEntry.getString("Name"));
                    if (blockName.equals("minecraft:air")) {
                        convertedPalette[i] = Block.AIR;
                    } else {
                        Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName));
                        // Properties
                        final Map<String, String> properties = new HashMap<>();
                        NBTCompound propertiesNBT = paletteEntry.getCompound("Properties");
                        if (propertiesNBT != null) {
                            for (var property : propertiesNBT) {
                                if (property.getValue().getID() != NBTType.TAG_String) {
                                    LOGGER.warn("Fail to parse block state properties {}, expected a TAG_String for {}, but contents were {}",
                                            propertiesNBT,
                                            property.getKey(),
                                            property.getValue().toSNBT());
                                } else {
                                    properties.put(property.getKey(), ((NBTString) property.getValue()).getValue());
                                }
                            }
                        }

                        if (!properties.isEmpty()) block = block.withProperties(properties);
                        // Handler
                        final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(block.name());
                        if (handler != null) block = block.withHandler(handler);

                        convertedPalette[i] = block;
                    }
                }

                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                        for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
                            try {
                                int blockIndex = y * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE + z * Chunk.CHUNK_SECTION_SIZE + x;
                                int paletteIndex = blockStateIndices[blockIndex];
                                Block block = convertedPalette[paletteIndex];

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

    private void loadBlockEntities(Chunk loadedChunk, ChunkReader chunkReader) {
        for (NBTCompound te : chunkReader.getBlockEntities()) {
            final var x = te.getInt("x");
            final var y = te.getInt("y");
            final var z = te.getInt("z");
            if (x == null || y == null || z == null) {
                LOGGER.warn("Tile entity has failed to load due to invalid coordinate");
                continue;
            }
            Block block = loadedChunk.getBlock(x, y, z);

            final String tileEntityID = te.getString("id");
            if (tileEntityID != null) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandlerOrDummy(tileEntityID);
                block = block.withHandler(handler);
            }
            // Remove anvil tags
            MutableNBTCompound mutableCopy = te.toMutableCompound();
            mutableCopy.remove("id");
            mutableCopy.remove("x");
            mutableCopy.remove("y");
            mutableCopy.remove("z");
            mutableCopy.remove("keepPacked");
            // Place block
            final var finalBlock = mutableCopy.getSize() > 0 ?
                    block.withNbt(mutableCopy.toCompound()) : block;
            loadedChunk.setBlock(x, y, z, finalBlock);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance(@NotNull Instance instance) {
        final var nbt = instance.tagHandler().asCompound();
        if (nbt.isEmpty()) {
            // Instance has no data
            return AsyncUtils.VOID_FUTURE;
        }
        try (NBTWriter writer = new NBTWriter(Files.newOutputStream(levelPath))) {
            writer.writeNamed("", nbt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return AsyncUtils.VOID_FUTURE;
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        RegionFile mcaFile;
        synchronized (alreadyLoaded) {
            mcaFile = getMCAFile(chunk.instance, chunkX, chunkZ);
            if (mcaFile == null) {
                final int regionX = CoordinatesKt.chunkToRegion(chunkX);
                final int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
                final String n = RegionFile.Companion.createFileName(regionX, regionZ);
                File regionFile = new File(regionPath.toFile(), n);
                try {
                    if (!regionFile.exists()) {
                        if (!regionFile.getParentFile().exists()) {
                            regionFile.getParentFile().mkdirs();
                        }
                        regionFile.createNewFile();
                    }
                    mcaFile = new RegionFile(new RandomAccessFile(regionFile, "rw"), regionX, regionZ);
                    alreadyLoaded.put(n, mcaFile);
                } catch (AnvilException | IOException e) {
                    LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
                    MinecraftServer.getExceptionManager().handleException(e);
                    return AsyncUtils.VOID_FUTURE;
                }
            }
        }
        ChunkWriter writer = new ChunkWriter(SupportedVersion.Companion.getLatest());
        save(chunk, writer);
        try {
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeColumnData(writer.toNBT(), chunk.getChunkX(), chunk.getChunkZ());
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
            return AsyncUtils.VOID_FUTURE;
        }
        return AsyncUtils.VOID_FUTURE;
    }

    private BlockState getBlockState(final Block block) {
        return blockStateId2ObjectCacheTLS.get().computeIfAbsent(block.stateId(), _unused -> new BlockState(block.name(), block.properties()));
    }

    private void save(Chunk chunk, ChunkWriter chunkWriter) {
        final int minY = chunk.getMinSection()*Chunk.CHUNK_SECTION_SIZE;
        final int maxY = chunk.getMaxSection()*Chunk.CHUNK_SECTION_SIZE -1;
        chunkWriter.setYPos(minY);
        List<NBTCompound> blockEntities = new ArrayList<>();
        chunkWriter.setStatus(ChunkColumn.GenerationStatus.Full);

        List<NBTCompound> sectionData = new ArrayList<>((maxY - minY + 1) / Chunk.CHUNK_SECTION_SIZE);
        int[] palettedBiomes = new int[ChunkSection.Companion.getBiomeArraySize()];
        int[] palettedBlockStates = new int[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
        for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
            ChunkSectionWriter sectionWriter = new ChunkSectionWriter(SupportedVersion.Companion.getLatest(), (byte)sectionY);

            Section section = chunk.getSection(sectionY);
            sectionWriter.setSkyLights(section.getSkyLight());
            sectionWriter.setBlockLights(section.getBlockLight());

            BiomePalette biomePalette = new BiomePalette();
            BlockPalette blockPalette = new BlockPalette();
            for (int sectionLocalY = 0; sectionLocalY < Chunk.CHUNK_SECTION_SIZE; sectionLocalY++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                        final int y = sectionLocalY + sectionY * Chunk.CHUNK_SECTION_SIZE;

                        int blockIndex = x + sectionLocalY * 16 * 16 + z * 16;

                        final Block block = chunk.getBlock(x, y, z);

                        final BlockState hephaistosBlockState = getBlockState(block);
                        blockPalette.increaseReference(hephaistosBlockState);

                        palettedBlockStates[blockIndex] = blockPalette.getPaletteIndex(hephaistosBlockState);

                        // biome are stored for 4x4x4 volumes, avoid unnecessary work
                        if(x % 4 == 0 && sectionLocalY % 4 == 0 && z % 4 == 0) {
                            int biomeIndex = (x/4) + (sectionLocalY/4) * 4 * 4 + (z/4) * 4;
                            final Biome biome = chunk.getBiome(x, y, z);
                            final String biomeName = biome.name().asString();

                            biomePalette.increaseReference(biomeName);
                            palettedBiomes[biomeIndex] = biomePalette.getPaletteIndex(biomeName);
                        }

                        // Block entities
                        final BlockHandler handler = block.handler();
                        var originalNBT = block.nbt();
                        if (originalNBT != null || handler != null) {
                            MutableNBTCompound nbt = originalNBT != null ?
                                    originalNBT.toMutableCompound() : new MutableNBTCompound();

                            if (handler != null) {
                                nbt.setString("id", handler.getNamespaceId().asString());
                            }
                            nbt.setInt("x", x + Chunk.CHUNK_SIZE_X * chunk.getChunkX());
                            nbt.setInt("y", y);
                            nbt.setInt("z", z + Chunk.CHUNK_SIZE_Z * chunk.getChunkZ());
                            nbt.setByte("keepPacked", (byte) 0);
                            blockEntities.add(nbt.toCompound());
                        }
                    }
                }
            }

            sectionWriter.setPalettedBiomes(biomePalette, palettedBiomes);
            sectionWriter.setPalettedBlockStates(blockPalette, palettedBlockStates);

            sectionData.add(sectionWriter.toNBT());
        }

        chunkWriter.setSectionsData(NBT.List(NBTType.TAG_Compound, sectionData));
        chunkWriter.setBlockEntityData(NBT.List(NBTType.TAG_Compound, blockEntities));
    }

    /**
     * Unload a given chunk. Also unloads a region when no chunk from that region is loaded.
     * @param chunk the chunk to unload
     */
    @Override
    public void unloadChunk(Chunk chunk) {
        final int regionX = CoordinatesKt.chunkToRegion(chunk.chunkX);
        final int regionZ = CoordinatesKt.chunkToRegion(chunk.chunkZ);

        final IntIntImmutablePair regionKey = new IntIntImmutablePair(regionX, regionZ);
        synchronized (perRegionLoadedChunks) {
            Set<IntIntImmutablePair> chunks = perRegionLoadedChunks.get(regionKey);
            if(chunks != null) { // if null, trying to unload a chunk from a region that was not created by the AnvilLoader
                // don't check return value, trying to unload a chunk not created by the AnvilLoader is valid
                chunks.remove(new IntIntImmutablePair(chunk.chunkX, chunk.chunkZ));

                if(chunks.isEmpty()) {
                    perRegionLoadedChunks.remove(regionKey);
                    RegionFile regionFile = alreadyLoaded.remove(RegionFile.Companion.createFileName(regionX, regionZ));
                    if(regionFile != null) {
                        try {
                            regionFile.close();
                        } catch (IOException e) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        }
                    }
                }
            }
        };
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
