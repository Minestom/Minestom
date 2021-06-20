package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jglrxavpok.hephaistos.mca.*;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AnvilLoader(Path path) {
        this.path = path;
    }

    public AnvilLoader(String path) {
        this(Path.of(path));
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, ChunkCallback callback) {
        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ);
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
        if (mcaFile != null) {
            ChunkColumn fileChunk = mcaFile.getChunk(chunkX, chunkZ);
            if (fileChunk != null) {
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
                placeBlocks(chunk, fileChunk);
                loadTileEntities(chunk, fileChunk);
                if (callback != null) {
                    callback.accept(chunk);
                }
                return chunk;
            }
        }
        return null;
    }

    private RegionFile getMCAFile(int chunkX, int chunkZ) {
        int regionX = CoordinatesKt.chunkToRegion(chunkX);
        int regionZ = CoordinatesKt.chunkToRegion(chunkZ);
        return alreadyLoaded.computeIfAbsent(RegionFile.Companion.createFileName(regionX, regionZ), n -> {
            try {
                final Path regionPath = path.resolve("region").resolve(n);
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
        // TODO
        callback.run();
    }
}
