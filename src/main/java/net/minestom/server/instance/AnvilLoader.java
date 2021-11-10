package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.exception.ExceptionManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mca.*;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
            instance.setTag(Tag.NBT, tag);
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
        for (var chunkSection : fileChunk.getSections()) {
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
        for (var section : fileChunk.getSections()) {
            if (section.getEmpty()) continue;
            final int yOffset = Chunk.CHUNK_SECTION_SIZE * section.getY();
            for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
                for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                    for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                        try {
                            final BlockState blockState = section.get(x, y, z);
                            final String blockName = blockState.getName();
                            if (blockName.equals("minecraft:air")) continue;
                            Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName));
                            // Properties
                            final Map<String, String> properties = blockState.getProperties();
                            if (!properties.isEmpty()) block = block.withProperties(properties);
                            // Handler
                            final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(block.name());
                            if (handler != null) block = block.withHandler(handler);

                            chunk.setBlock(x, y + yOffset, z, block);
                        } catch (Exception e) {
                            EXCEPTION_MANAGER.handleException(e);
                        }
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
                final BlockHandler handler = BLOCK_MANAGER.getHandlerOrDummy(tileEntityID);
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

    @Override
    public @NotNull CompletableFuture<Void> saveInstance(@NotNull Instance instance) {
        final var nbt = instance.getTag(Tag.NBT);
        if (nbt == null) {
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
                    return AsyncUtils.VOID_FUTURE;
                }
            }
        }
        ChunkColumn column;
        try {
            column = mcaFile.getOrCreateChunk(chunkX, chunkZ);
        } catch (AnvilException | IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            EXCEPTION_MANAGER.handleException(e);
            return AsyncUtils.VOID_FUTURE;
        }
        save(chunk, column);
        try {
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
            mcaFile.writeColumn(column);
            mcaFile.forget(column);
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk " + chunkX + ", " + chunkZ, e);
            EXCEPTION_MANAGER.handleException(e);
            return AsyncUtils.VOID_FUTURE;
        }
        return AsyncUtils.VOID_FUTURE;
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
