package net.minestom.server.instance;

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
        loadTileEntities(chunk, chunkReader);
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
            Section section = chunk.getSection(sectionReader.getY());

            if(sectionReader.getSkyLight() != null) {
                section.setSkyLight(sectionReader.getSkyLight().copyArray());
            }
            if(sectionReader.getBlockLight() != null) {
                section.setBlockLight(sectionReader.getBlockLight().copyArray());
            }

            if (sectionReader.isSectionEmpty()) continue;
            final int sectionY = sectionReader.getY();
            final int yOffset = Chunk.CHUNK_SECTION_SIZE * sectionY;

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

    private void loadTileEntities(Chunk loadedChunk, ChunkReader chunkReader) {
        for (NBTCompound te : chunkReader.getTileEntities()) {
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
        ChunkColumn column;
        try {
            column = mcaFile.getOrCreateChunk(chunkX, chunkZ);
        } catch (AnvilException | IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
            return AsyncUtils.VOID_FUTURE;
        }
        save(chunk, column);
        try {
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeColumn(column);
            mcaFile.forget(column);
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
            return AsyncUtils.VOID_FUTURE;
        }
        return AsyncUtils.VOID_FUTURE;
    }

    private void save(Chunk chunk, ChunkColumn chunkColumn) {
        chunkColumn.changeVersion(SupportedVersion.Companion.getLatest());
        chunkColumn.setYRange(chunk.getMinSection()*16, chunk.getMaxSection()*16-1);
        List<NBTCompound> tileEntities = new ArrayList<>();
        chunkColumn.setGenerationStatus(ChunkColumn.GenerationStatus.Full);
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = chunkColumn.getMinY(); y < chunkColumn.getMaxY(); y++) {
                    final Block block = chunk.getBlock(x, y, z);
                    // Block
                    chunkColumn.setBlockState(x, y, z, new BlockState(block.name(), block.properties()));
                    chunkColumn.setBiome(x, y, z, chunk.getBiome(x, y, z).name().asString());

                    // Tile entity
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
                        tileEntities.add(nbt.toCompound());
                    }
                }
            }
        }
        chunkColumn.setTileEntities(NBT.List(NBTType.TAG_Compound, tileEntities));
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
