package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.mca.*;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
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
import java.util.concurrent.ConcurrentHashMap;

public class AnvilLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilLoader.class);
    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();
    private static final Biome BIOME = Biome.PLAINS;

    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();
    private final Path path;
    private final Path regionFolder;

    public AnvilLoader(@NotNull Path path) {
        this.path = path;
        this.regionFolder = path.resolve("region");
    }

    public AnvilLoader(@NotNull String path) {
        this(Path.of(path));
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, ChunkCallback callback) {
        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ);
        if (!Files.exists(path)) {
            // No world folder
            return false;
        }
        try {
            Chunk chunk = loadMCA(instance, chunkX, chunkZ, callback);
            return chunk != null;
        } catch (IOException | AnvilException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Chunk loadMCA(Instance instance, int chunkX, int chunkZ, ChunkCallback callback) throws IOException, AnvilException {
        RegionFile mcaFile = getMCAFile(chunkX, chunkZ);
        if (mcaFile == null)
            return null;
        ChunkColumn fileChunk = mcaFile.getChunk(chunkX, chunkZ);
        if (fileChunk == null)
            return null;

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
        {
            placeBlocks(chunk, fileChunk);
            loadTileEntities(chunk, fileChunk);
        }

        // Lights
        {
            final var chunkSections = fileChunk.getSections();
            for (var chunkSection : chunkSections) {
                Section section = chunk.getSection(chunkSection.getY());
                section.setSkyLight(chunkSection.getSkyLights());
                section.setBlockLight(chunkSection.getBlockLights());
            }
        }

        if (callback != null) {
            callback.accept(chunk);
        }
        return chunk;
    }

    private RegionFile getMCAFile(int chunkX, int chunkZ) {
        final int regionX = CoordinatesKt.chunkToRegion(chunkX);
        final int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
        return alreadyLoaded.computeIfAbsent(RegionFile.Companion.createFileName(regionX, regionZ), n -> {
            try {
                final Path regionPath = regionFolder.resolve(n);
                if (!Files.exists(regionPath)) {
                    return null;
                }
                return new RegionFile(new RandomAccessFile(regionPath.toFile(), "rw"), regionX, regionZ);
            } catch (IOException | AnvilException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private void loadTileEntities(Chunk loadedChunk, ChunkColumn fileChunk) {
        for (NBTCompound te : fileChunk.getTileEntities()) {
            final String tileEntityID = te.getString("id");
            final int x = te.getInt("x") + loadedChunk.getChunkX() * 16;
            final int y = te.getInt("y");
            final int z = te.getInt("z") + loadedChunk.getChunkZ() * 16;
            if (tileEntityID != null) {
                // TODO load BlockHandler and place
            }
        }
    }

    private void placeBlocks(Chunk chunk, ChunkColumn fileChunk) {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < 256; y++) { // TODO don't hardcode height
                    try {
                        // TODO: are there block entities here?
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
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // TODO: find a way to unload MCAFiles when an entire region is unloaded

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        RegionFile mcaFile;
        synchronized (alreadyLoaded) {
            mcaFile = getMCAFile(chunkX, chunkZ);
            if (mcaFile == null) {
                final int regionX = CoordinatesKt.chunkToRegion(chunkX);
                final int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
                final String n = RegionFile.Companion.createFileName(regionX, regionZ);
                File regionFile = new File(regionFolder.toFile(), n);
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
                    e.printStackTrace();
                    return;
                }
            }
        }
        ChunkColumn column;
        try {
            column = mcaFile.getOrCreateChunk(chunkX, chunkZ);
        } catch (AnvilException | IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            e.printStackTrace();
            return;
        }
        save(chunk, column);

        try {
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeColumn(column);
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            e.printStackTrace();
            return;
        }

        if (callback != null)
            callback.run();
    }

    private void saveTileEntities(Chunk chunk, ChunkColumn fileChunk) {
        /*NBTList<NBTCompound> tileEntities = new NBTList<>(NBTTypes.TAG_Compound);
        for (var index : chunk.getBlockEntities()) {
            int x = ChunkUtils.blockIndexToChunkPositionX(index);
            int y = ChunkUtils.blockIndexToChunkPositionY(index);
            int z = ChunkUtils.blockIndexToChunkPositionZ(index);
            position.setX(x);
            position.setY(y);
            position.setZ(z);
            CustomBlock customBlock = chunk.getCustomBlock(x, y, z);
            if (customBlock instanceof VanillaBlock) {
                NBTCompound nbt = new NBTCompound();
                nbt.setInt("x", x);
                nbt.setInt("y", y);
                nbt.setInt("z", z);
                nbt.setByte("keepPacked", (byte) 0);
                Block block = Block.fromStateId(customBlock.getDefaultBlockStateId());
                Data data = chunk.getBlockData(ChunkUtils.getBlockIndex(x, y, z));
                customBlock.writeBlockEntity(position, data, nbt);
                if (block.hasBlockEntity()) {
                    nbt.setString("id", block.getBlockEntityName().toString());
                    tileEntities.add(nbt);
                } else {
                    LOGGER.warn("Tried to save block entity for a block which is not a block entity? Block is {} at {},{},{}", customBlock, x, y, z);
                }
            }
        }
        fileChunk.setTileEntities(tileEntities);*/
    }

    private void save(Chunk chunk, ChunkColumn chunkColumn) {
        chunkColumn.setGenerationStatus(ChunkColumn.GenerationStatus.Full);

        // TODO: other elements to save
        saveTileEntities(chunk, chunkColumn);

        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < 256; y++) { // TODO don't hardcode world height
                    final Block block = chunk.getBlock(x, y, z);
                    BlockState state = new BlockState(block.name(), block.properties());
                    chunkColumn.setBlockState(x, y, z, state);

                    int index = ((y >> 2) & 63) << 4 | ((z >> 2) & 3) << 2 | ((x >> 2) & 3); // https://wiki.vg/Chunk_Format#Biomes
                    Biome biome = chunk.getBiomes()[index];
                    chunkColumn.setBiome(x, 0, z, biome.getId());
                }
            }
        }
    }
}
