package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mca.*;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AnvilLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilLoader.class);
    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();
    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();
    private static final ExceptionManager EXCEPTION_MANAGER = MinecraftServer.getExceptionManager();
    private static final Biome BIOME = Biome.PLAINS;

    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();
    private final Path path;
    private final Path regionPath;

    public AnvilLoader(@NotNull Path path) {
        this.path = path;
        this.regionPath = path.resolve("region");
    }

    public AnvilLoader(@NotNull String path) {
        this(Path.of(path));
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
        } catch (IOException | AnvilException e) {
            EXCEPTION_MANAGER.handleException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private @NotNull CompletableFuture<@Nullable Chunk> loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException, AnvilException {
        final RegionFile mcaFile = getMCAFile(chunkX, chunkZ);
        if (mcaFile == null)
            return CompletableFuture.completedFuture(null);
        final ChunkColumn fileChunk = mcaFile.getChunk(chunkX, chunkZ);
        if (fileChunk == null)
            return CompletableFuture.completedFuture(null);

        Biome[] biomes;
        if (fileChunk.getGenerationStatus().compareTo(ChunkColumn.GenerationStatus.Biomes) > 0) {
            int[] fileChunkBiomes = fileChunk.getBiomes();
            biomes = new Biome[fileChunkBiomes.length];
            for (int i = 0; i < fileChunkBiomes.length; i++) {
                final int id = fileChunkBiomes[i];
                biomes[i] = Objects.requireNonNullElse(BIOME_MANAGER.getById(id), BIOME);
            }
        } else {
            biomes = new Biome[1024]; // TODO don't hardcode
            Arrays.fill(biomes, BIOME);
        }
        Chunk chunk = new DynamicChunk(instance, biomes, chunkX, chunkZ);
        // Blocks
        loadBlocks(chunk, fileChunk);
        loadTileEntities(chunk, fileChunk);
        // Lights
        final var chunkSections = fileChunk.getSections();
        for (var chunkSection : chunkSections) {
            Section section = chunk.getSection(chunkSection.getY());
            section.setSkyLight(chunkSection.getSkyLights());
            section.setBlockLight(chunkSection.getBlockLights());
        }
        mcaFile.forget(fileChunk);
        return CompletableFuture.completedFuture(chunk);
    }

    private @Nullable RegionFile getMCAFile(int chunkX, int chunkZ) {
        final int regionX = CoordinatesKt.chunkToRegion(chunkX);
        final int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
        return alreadyLoaded.computeIfAbsent(RegionFile.Companion.createFileName(regionX, regionZ), n -> {
            try {
                final Path regionPath = this.regionPath.resolve(n);
                if (!Files.exists(regionPath)) {
                    return null;
                }
                return new RegionFile(new RandomAccessFile(regionPath.toFile(), "rw"), regionX, regionZ);
            } catch (IOException | AnvilException e) {
                EXCEPTION_MANAGER.handleException(e);
                return null;
            }
        });
    }

    private void loadBlocks(Chunk chunk, ChunkColumn fileChunk) {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < 256; y++) { // TODO don't hardcode height
                    try {
                        final BlockState blockState = fileChunk.getBlockState(x, y, z);
                        Block block = Block.fromNamespaceId(blockState.getName());
                        if (block == null) {
                            // Invalid block
                            continue;
                        }
                        final var properties = blockState.getProperties();
                        if (!properties.isEmpty()) {
                            block = block.withProperties(properties);
                        }
                        chunk.setBlock(x, y, z, block);
                    } catch (Exception e) {
                        EXCEPTION_MANAGER.handleException(e);
                    }
                }
            }
        }
    }

    private void loadTileEntities(Chunk loadedChunk, ChunkColumn fileChunk) {
        for (NBTCompound te : fileChunk.getTileEntities()) {
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
                var handler = BLOCK_MANAGER.getHandler(tileEntityID);
                if (handler == null) {
                    LOGGER.warn("Block {} does not have any corresponding handler, default to dummy.", tileEntityID);
                    handler = BlockHandler.Dummy.get(tileEntityID);
                }
                block = block.withHandler(handler);
            }
            // Remove anvil tags
            te.removeTag("id")
                    .removeTag("x").removeTag("y").removeTag("z")
                    .removeTag("keepPacked");
            // Place block
            final var finalBlock = te.getSize() > 0 ?
                    block.withNbt(te) : block;
            loadedChunk.setBlock(x, y, z, finalBlock);
        }
    }

    // TODO: find a way to unload MCAFiles when an entire region is unloaded


    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        RegionFile mcaFile;
        synchronized (alreadyLoaded) {
            mcaFile = getMCAFile(chunkX, chunkZ);
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
                    EXCEPTION_MANAGER.handleException(e);
                    return AsyncUtils.NULL_FUTURE;
                }
            }
        }
        ChunkColumn column;
        try {
            column = mcaFile.getOrCreateChunk(chunkX, chunkZ);
        } catch (AnvilException | IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            EXCEPTION_MANAGER.handleException(e);
            return AsyncUtils.NULL_FUTURE;
        }
        save(chunk, column);
        try {
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeColumn(column);
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            EXCEPTION_MANAGER.handleException(e);
            return AsyncUtils.NULL_FUTURE;
        }
        return AsyncUtils.NULL_FUTURE;
    }

    private void save(Chunk chunk, ChunkColumn chunkColumn) {
        NBTList<NBTCompound> tileEntities = new NBTList<>(NBTTypes.TAG_Compound);
        chunkColumn.setGenerationStatus(ChunkColumn.GenerationStatus.Full);
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < 256; y++) { // TODO don't hardcode world height
                    final Block block = chunk.getBlock(x, y, z);
                    // Block
                    BlockState state = new BlockState(block.name(), block.properties());
                    chunkColumn.setBlockState(x, y, z, state);
                    // Biome
                    int index = ((y >> 2) & 63) << 4 | ((z >> 2) & 3) << 2 | ((x >> 2) & 3); // https://wiki.vg/Chunk_Format#Biomes
                    Biome biome = chunk.getBiomes()[index];
                    chunkColumn.setBiome(x, 0, z, biome.getId());

                    // Tile entity
                    var nbt = block.nbt();
                    final BlockHandler handler = block.handler();
                    if (nbt != null || handler != null) {
                        nbt = Objects.requireNonNullElseGet(nbt, NBTCompound::new);
                        if (handler != null) {
                            nbt.setString("id", handler.getNamespaceId().asString());
                        }
                        nbt.setInt("x", x + Chunk.CHUNK_SIZE_X * chunk.getChunkX());
                        nbt.setInt("y", y);
                        nbt.setInt("z", z + Chunk.CHUNK_SIZE_Z * chunk.getChunkZ());
                        nbt.setByte("keepPacked", (byte) 0);
                        tileEntities.add(nbt);
                    }
                }
            }
        }
        chunkColumn.setTileEntities(tileEntities);
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
