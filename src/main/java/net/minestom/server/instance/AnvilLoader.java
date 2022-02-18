package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mca.*;
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
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private @NotNull CompletableFuture<@Nullable Chunk> loadMCA(Instance instance, int chunkX, int chunkZ) throws IOException, AnvilException {
        final RegionFile mcaFile = getMCAFile(instance, chunkX, chunkZ);
        if (mcaFile == null)
            return CompletableFuture.completedFuture(null);
        final ChunkColumn fileChunk = mcaFile.getChunk(chunkX, chunkZ);
        if (fileChunk == null)
            return CompletableFuture.completedFuture(null);

        Chunk chunk = new DynamicChunk(instance, chunkX, chunkZ);
        if(fileChunk.getMinY() < instance.getDimensionType().getMinY()) {
            throw new AnvilException(
                    String.format("Trying to load chunk with minY = %d, but instance dimension type (%s) has a minY of %d",
                            fileChunk.getMinY(),
                            instance.getDimensionType().getName().asString(),
                            instance.getDimensionType().getMinY()
                            ));
        }
        if(fileChunk.getMaxY() > instance.getDimensionType().getMaxY()) {
            throw new AnvilException(
                    String.format("Trying to load chunk with maxY = %d, but instance dimension type (%s) has a maxY of %d",
                            fileChunk.getMaxY(),
                            instance.getDimensionType().getName().asString(),
                            instance.getDimensionType().getMaxY()
                    ));
        }

        // TODO: Parallelize block, block entities and biome loading

        if (fileChunk.getGenerationStatus().compareTo(ChunkColumn.GenerationStatus.Biomes) > 0) {
            HashMap<String, Biome> biomeCache = new HashMap<>();

            for (ChunkSection section : fileChunk.getSections().values()) {
                if (section.getEmpty()) continue;
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                            int finalX = fileChunk.getX() * Chunk.CHUNK_SIZE_X + x;
                            int finalZ = fileChunk.getZ() * Chunk.CHUNK_SIZE_Z + z;
                            int finalY = section.getY() * Chunk.CHUNK_SECTION_SIZE + y;
                            String biomeName = section.getBiome(x, y, z);
                            Biome biome = biomeCache.computeIfAbsent(biomeName, n ->
                                    Objects.requireNonNullElse(MinecraftServer.getBiomeManager().getByName(NamespaceID.from(n)), BIOME));
                            chunk.setBiome(finalX, finalY, finalZ, biome);
                        }
                    }
                }
            }
        }
        // Blocks
        loadBlocks(chunk, fileChunk);
        loadTileEntities(chunk, fileChunk);
        // Lights
        for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
            var section = chunk.getSection(sectionY);
            var chunkSection = fileChunk.getSection((byte) sectionY);
            section.setSkyLight(chunkSection.getSkyLights());
            section.setBlockLight(chunkSection.getBlockLights());
        }
        mcaFile.forget(fileChunk);
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

    private void loadBlocks(Chunk chunk, ChunkColumn fileChunk) {
        for (var section : fileChunk.getSections().values()) {
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
                            MinecraftServer.getExceptionManager().handleException(e);
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
