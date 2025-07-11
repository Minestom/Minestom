package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.async.AsyncUtils;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.worldevent.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.coordinate.CoordConversion.*;
import static net.minestom.server.utils.chunk.ChunkUtils.isLoaded;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceContainer.class);

    private static final AnvilLoader DEFAULT_LOADER = new AnvilLoader("world");

    private static final BlockFace[] BLOCK_UPDATE_FACES = new BlockFace[]{
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.BOTTOM, BlockFace.TOP
    };

    // the shared instances assigned to this instance
    private final List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    // the chunk generator used, can be null
    private volatile Generator generator;
    // (chunk index -> chunk) map, contains all the chunks in the instance
    // used as a monitor when access is required
    private final Long2ObjectSyncMap<Chunk> chunks = Long2ObjectSyncMap.hashmap();
    private final Map<Long, CompletableFuture<Chunk>> loadingChunks = new ConcurrentHashMap<>();

    private final Lock changingBlockLock = new ReentrantLock();
    private final Map<BlockVec, Block> currentlyChangingBlocks = new HashMap<>();

    // the chunk loader, used when trying to load/save a chunk from another source
    private IChunkLoader chunkLoader;

    // used to automatically enable the chunk loading or not
    private boolean autoChunkLoad = true;

    // used to supply a new chunk object at a position when requested
    private ChunkSupplier chunkSupplier;

    // Fields for instance copy
    protected InstanceContainer srcInstance; // only present if this instance has been created using a copy
    private long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)

    public InstanceContainer(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType) {
        this(uuid, dimensionType, null, dimensionType.key());
    }

    public InstanceContainer(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType, @NotNull Key dimensionName) {
        this(uuid, dimensionType, null, dimensionName);
    }

    public InstanceContainer(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType, @Nullable IChunkLoader loader) {
        this(uuid, dimensionType, loader, dimensionType.key());
    }

    public InstanceContainer(@NotNull UUID uuid, @NotNull RegistryKey<DimensionType> dimensionType, @Nullable IChunkLoader loader, @NotNull Key dimensionName) {
        this(MinecraftServer.getDimensionTypeRegistry(), uuid, dimensionType, loader, dimensionName);
    }

    public InstanceContainer(
            @NotNull DynamicRegistry<DimensionType> dimensionTypeRegistry,
            @NotNull UUID uuid,
            @NotNull RegistryKey<DimensionType> dimensionType,
            @Nullable IChunkLoader loader,
            @NotNull Key dimensionName
    ) {
        super(dimensionTypeRegistry, uuid, dimensionType, dimensionName);
        setChunkSupplier(DynamicChunk::new);
        setChunkLoader(Objects.requireNonNullElse(loader, DEFAULT_LOADER));
        this.chunkLoader.loadInstance(this);
        // last block change starts at instance creation time
        refreshLastBlockChangeTime();
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block, boolean doBlockUpdates) {
        Chunk chunk = getChunkAt(x, z);
        if (chunk == null) {
            Check.stateCondition(!hasEnabledAutoChunkLoad(),
                    "Tried to set a block to an unloaded chunk with auto chunk load disabled");
            chunk = loadChunk(globalToChunk(x), globalToChunk(z)).join();
        }
        if (isLoaded(chunk)) UNSAFE_setBlock(chunk, x, y, z, block, null, null, doBlockUpdates, 0);
    }

    /**
     * Sets a block at the specified position.
     * <p>
     * Unsafe because the method is not synchronized and it does not verify if the chunk is loaded or not.
     *
     * @param chunk the {@link Chunk} which should be loaded
     * @param x     the block X
     * @param y     the block Y
     * @param z     the block Z
     * @param block the block to place
     */
    private synchronized void UNSAFE_setBlock(@NotNull Chunk chunk, int x, int y, int z, @NotNull Block block,
                                              @Nullable BlockHandler.Placement placement, @Nullable BlockHandler.Destroy destroy,
                                              boolean doBlockUpdates, int updateDistance) {
        if (chunk.isReadOnly()) return;
        final DimensionType dim = getCachedDimensionType();
        if (y >= dim.maxY() || y < dim.minY()) {
            LOGGER.warn("tried to set a block outside the world bounds, should be within [{}, {}): {}", dim.minY(), dim.maxY(), y);
            return;
        }

        synchronized (chunk) {
            // Refresh the last block change time
            this.lastBlockChangeTime = System.nanoTime();
            final BlockVec blockPosition = new BlockVec(x, y, z);
            if (isAlreadyChanged(blockPosition, block)) { // do NOT change the block again.
                // Avoids StackOverflowExceptions when onDestroy tries to destroy the block itself
                // This can happen with nether portals which break the entire frame when a portal block is broken
                return;
            }
            this.currentlyChangingBlocks.put(blockPosition, block);

            // Change id based on neighbors
            final BlockPlacementRule blockPlacementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(block);
            if (placement != null && blockPlacementRule != null && doBlockUpdates) {
                BlockPlacementRule.PlacementState rulePlacement;
                if (placement instanceof BlockHandler.PlayerPlacement pp) {
                    rulePlacement = new BlockPlacementRule.PlacementState(
                            this, block, pp.getBlockFace(), blockPosition,
                            new Vec(pp.getCursorX(), pp.getCursorY(), pp.getCursorZ()),
                            pp.getPlayer().getPosition(),
                            pp.getPlayer().getItemInHand(pp.getHand()),
                            pp.getPlayer().isSneaking()
                    );
                } else {
                    rulePlacement = new BlockPlacementRule.PlacementState(
                            this, block, null, blockPosition,
                            null, null, null,
                            false
                    );
                }

                block = blockPlacementRule.blockPlace(rulePlacement);
                if (block == null) block = Block.AIR;
            }

            // Set the block
            chunk.setBlock(x, y, z, block, placement, destroy);

            // Refresh neighbors since a new block has been placed
            if (doBlockUpdates) {
                executeNeighboursBlockPlacementRule(blockPosition, updateDistance);
            }

            // Refresh player chunk block
            {
                chunk.sendPacketToViewers(new BlockChangePacket(blockPosition, block.stateId()));
                RegistryData.BlockEntry registry = block.registry();
                if (registry.isBlockEntity()) {
                    final CompoundBinaryTag data = BlockUtils.extractClientNbt(block);
                    chunk.sendPacketToViewers(new BlockEntityDataPacket(blockPosition, registry.blockEntityId(), data));
                }
            }
            EventDispatcher.call(new InstanceBlockUpdateEvent(this, blockPosition, block));
        }
    }

    @Override
    public @NotNull BlockBatch getBlockBatch(long flags, @NotNull Point origin, @NotNull Point p1, @NotNull Point p2) {
        final int originX = origin.blockX(), originY = origin.blockY(), originZ = origin.blockZ();
        final boolean originAligned = sectionAligned(originX, originY, originZ);
        final int minX = Math.min(p1.blockX(), p2.blockX());
        final int minY = Math.min(p1.blockY(), p2.blockY());
        final int minZ = Math.min(p1.blockZ(), p2.blockZ());
        final int maxX = Math.max(p1.blockX(), p2.blockX());
        final int maxY = Math.max(p1.blockY(), p2.blockY());
        final int maxZ = Math.max(p1.blockZ(), p2.blockZ());

        final int minSectionX = globalToChunk(minX), minSectionY = globalToChunk(minY), minSectionZ = globalToChunk(minZ);
        final int maxSectionX = globalToChunk(maxX), maxSectionY = globalToChunk(maxY), maxSectionZ = globalToChunk(maxZ);

        LongSet sectionIndexes = new LongOpenHashSet();
        Set<BlockVec> blockCoords = new HashSet<>();

        // Iterate through all sections in the bounding box
        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                    // Calculate section bounds in global coordinates
                    final int sectionMinX = sectionX * 16;
                    final int sectionMinY = sectionY * 16;
                    final int sectionMinZ = sectionZ * 16;
                    final int sectionMaxX = sectionMinX + 15;
                    final int sectionMaxY = sectionMinY + 15;
                    final int sectionMaxZ = sectionMinZ + 15;

                    // Check if this section is fully contained within the requested bounds
                    if (sectionMinX >= minX && sectionMaxX <= maxX &&
                            sectionMinY >= minY && sectionMaxY <= maxY &&
                            sectionMinZ >= minZ && sectionMaxZ <= maxZ) {
                        // Section is fully contained - add to sectionIndexes
                        sectionIndexes.add(sectionIndex(sectionX, sectionY, sectionZ));
                    } else {
                        // Section is partially contained - add individual blocks to blockCoords
                        final int blockMinX = Math.max(sectionMinX, minX);
                        final int blockMaxX = Math.min(sectionMaxX, maxX);
                        final int blockMinY = Math.max(sectionMinY, minY);
                        final int blockMaxY = Math.min(sectionMaxY, maxY);
                        final int blockMinZ = Math.max(sectionMinZ, minZ);
                        final int blockMaxZ = Math.min(sectionMaxZ, maxZ);

                        for (int x = blockMinX; x <= blockMaxX; x++) {
                            for (int y = blockMinY; y <= blockMaxY; y++) {
                                for (int z = blockMinZ; z <= blockMaxZ; z++) {
                                    blockCoords.add(new BlockVec(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
        }

        final boolean ignoreData = (flags & BlockBatch.IGNORE_DATA_FLAG) != 0;
        final boolean generate = (flags & BlockBatch.GENERATE_FLAG) != 0;
        Function<BlockBatch.Builder, Void> chunkRegister = builder -> {
            for (long sectionIdx : sectionIndexes) {
                final int sectionX = sectionIndexGetX(sectionIdx);
                final int sectionY = sectionIndexGetY(sectionIdx);
                final int sectionZ = sectionIndexGetZ(sectionIdx);
                Chunk chunk = getChunk(sectionX, sectionZ);
                if (chunk == null) {
                    if (!generate) continue;
                    chunk = loadOptionalChunk(sectionX, sectionZ).join();
                }
                synchronized (chunk) {
                    Section section = chunk.getSection(sectionY);
                    Palette palette = section.blockPalette();
                    if (originAligned) {
                        final int offsetX = origin.chunkX();
                        final int offsetY = origin.section();
                        final int offsetZ = origin.chunkZ();
                        builder.copyPalette(sectionX - offsetX, sectionY - offsetY, sectionZ - offsetZ, palette);
                    } else {
                        // Unaligned: copy palette with offset
                        palette.getAll((x, y, z, value) -> {
                            final int globalX = (sectionX * 16) + x;
                            final int globalY = (sectionY * 16) + y;
                            final int globalZ = (sectionZ * 16) + z;
                            if (globalX < minX || globalX > maxX ||
                                    globalY < minY || globalY > maxY ||
                                    globalZ < minZ || globalZ > maxZ) {
                                return; // Skip blocks outside the requested bounds
                            }
                            final int bX = globalX - originX, bY = globalY - originY, bZ = globalZ - originZ;
                            final Block block = Block.fromStateId(value);
                            assert block != null;
                            builder.setBlock(bX, bY, bZ, block);
                        });
                    }
                    if (!ignoreData && chunk instanceof DynamicChunk dynamicChunk) {
                        // Add block states
                        for (Int2ObjectMap.Entry<Block> entry : dynamicChunk.entries.int2ObjectEntrySet()) {
                            final int blockIndex = entry.getIntKey();
                            final int localX = chunkBlockIndexGetX(blockIndex), localY = chunkBlockIndexGetY(blockIndex), localZ = chunkBlockIndexGetZ(blockIndex);
                            final int blockSectionY = floorSection(localY);
                            if (blockSectionY != sectionY) continue;
                            final int globalX = (sectionX * 16) + localX, globalY = (sectionY * 16) + localY, globalZ = (sectionZ * 16) + localZ;
                            final int bX = globalX - originX, bY = globalY - originY, bZ = globalZ - originZ;
                            final Block block = entry.getValue();
                            builder.setBlock(bX, bY, bZ, block);
                        }
                    }
                }
            }
            return null;
        };

        if (blockCoords.isEmpty()) {
            // Fast aligned batch
            return BlockBatch.aligned(chunkRegister::apply);
        } else {
            // Slower unaligned batch
            return BlockBatch.unaligned(builder -> {
                chunkRegister.apply(builder);
                // Add individual blocks from partially contained sections
                final Condition condition = ignoreData ? Condition.TYPE : Condition.NONE;
                for (BlockVec vec : blockCoords) {
                    final int bX = vec.blockX() - originX, bY = vec.blockY() - originY, bZ = vec.blockZ() - originZ;
                    try {
                        final Block block = getBlock(vec, condition);
                        builder.setBlock(bX, bY, bZ, block);
                    } catch (NullPointerException ignored) {
                    }
                }
            });
        }
    }

    @Override
    public void setBlockBatch(int x, int y, int z, @NotNull BlockBatch batch) {
        final BlockBatchImpl batchImpl = (BlockBatchImpl) batch;
        LongSet chunkIndexes = new LongOpenHashSet();
        if (sectionAligned(x, y, z)) {
            setBlockBatchAligned(x, y, z, batchImpl, chunkIndexes);
        } else {
            setBlockBatchUnaligned(x, y, z, batchImpl, chunkIndexes);
        }
        // Invalidate all affected chunks
        for (long chunkIndex : chunkIndexes) {
            final int chunkX = chunkIndexGetX(chunkIndex);
            final int chunkZ = chunkIndexGetZ(chunkIndex);
            final Chunk chunk = getChunk(chunkX, chunkZ);
            if (chunk == null) continue;
            chunk.invalidate();
            chunk.sendChunk();
        }
    }

    private void setBlockBatchAligned(int x, int y, int z, BlockBatchImpl batch, LongSet chunkIndexes) {
        // Each batch section map to a single instance section
        for (Long2ObjectMap.Entry<BlockBatchImpl.SectionState> entry : batch.sectionStates().long2ObjectEntrySet()) {
            final long sectionIndex = entry.getLongKey();
            final BlockBatchImpl.SectionState sectionState = entry.getValue();

            // Extract section coordinates from the batch
            final int batchSectionX = sectionIndexGetX(sectionIndex);
            final int batchSectionY = sectionIndexGetY(sectionIndex);
            final int batchSectionZ = sectionIndexGetZ(sectionIndex);

            // Calculate target section coordinates with offset
            final int targetSectionX = batchSectionX + globalToChunk(x);
            final int targetSectionY = batchSectionY + globalToChunk(y);
            final int targetSectionZ = batchSectionZ + globalToChunk(z);

            // Get the target chunk
            final Chunk targetChunk = batch.generate() ?
                    loadOptionalChunk(targetSectionX, targetSectionZ).join() : getChunk(targetSectionX, targetSectionZ);
            if (targetChunk == null) continue;
            chunkIndexes.add(chunkIndex(targetSectionX, targetSectionZ));
            synchronized (targetChunk) {
                final Section targetSection = targetChunk.getSection(targetSectionY);
                if (batch.aligned()) {
                    clearSectionNbtData(targetChunk, targetSectionX, targetSectionY, targetSectionZ);
                    // Section-aligned: direct palette copy
                    targetSection.blockPalette().copyFrom(sectionState.palette());
                } else {
                    // For non-section-aligned, clear NBT for specific blocks being overwritten
                    sectionState.palette().getAllPresent((localX, localY, localZ, value) -> {
                        final int globalBlockX = (targetSectionX * 16) + localX;
                        final int globalBlockY = (targetSectionY * 16) + localY;
                        final int globalBlockZ = (targetSectionZ * 16) + localZ;
                        clearBlockNbtData(targetChunk, globalBlockX, globalBlockY, globalBlockZ);
                        targetSection.blockPalette().set(localX, localY, localZ, value - 1);
                    });
                }

                // Handle block states if present (for blocks with NBT or handlers)
                if (!batch.ignoreData()) {
                    // For blocks with NBT or handlers, we still need to set them individually
                    // as palette copy only handles the state IDs
                    for (Int2ObjectMap.Entry<Block> blockEntry : sectionState.blockStates().int2ObjectEntrySet()) {
                        final int blockIndex = blockEntry.getIntKey();
                        final Block block = blockEntry.getValue();

                        // Convert section block index back to coordinates
                        final int localX = sectionBlockIndexGetX(blockIndex);
                        final int localY = sectionBlockIndexGetY(blockIndex);
                        final int localZ = sectionBlockIndexGetZ(blockIndex);

                        final int globalBlockX = (batchSectionX * 16) + localX + x;
                        final int globalBlockY = (batchSectionY * 16) + localY + y;
                        final int globalBlockZ = (batchSectionZ * 16) + localZ + z;

                        setBlock(globalBlockX, globalBlockY, globalBlockZ, block);
                    }
                }
            }
        }
    }

    private void setBlockBatchUnaligned(int x, int y, int z, BlockBatchImpl batch, LongSet chunkIndexes) {
        // For unaligned batches, a single batch section can affect multiple instance sections
        for (Long2ObjectMap.Entry<BlockBatchImpl.SectionState> entry : batch.sectionStates().long2ObjectEntrySet()) {
            final long sectionIndex = entry.getLongKey();
            final BlockBatchImpl.SectionState sectionState = entry.getValue();

            // Extract section coordinates from the batch
            final int batchSectionX = sectionIndexGetX(sectionIndex);
            final int batchSectionY = sectionIndexGetY(sectionIndex);
            final int batchSectionZ = sectionIndexGetZ(sectionIndex);

            // Calculate global coordinates of this batch section
            final int globalSectionX = batchSectionX * 16 + x;
            final int globalSectionY = batchSectionY * 16 + y;
            final int globalSectionZ = batchSectionZ * 16 + z;

            // Find all instance sections that this batch section affects
            final int minInstanceSectionX = globalToChunk(globalSectionX);
            final int maxInstanceSectionX = globalToChunk(globalSectionX + 15);
            final int minInstanceSectionY = globalToChunk(globalSectionY);
            final int maxInstanceSectionY = globalToChunk(globalSectionY + 15);
            final int minInstanceSectionZ = globalToChunk(globalSectionZ);
            final int maxInstanceSectionZ = globalToChunk(globalSectionZ + 15);

            // Iterate through all affected instance sections
            for (int instanceSectionX = minInstanceSectionX; instanceSectionX <= maxInstanceSectionX; instanceSectionX++) {
                for (int instanceSectionY = minInstanceSectionY; instanceSectionY <= maxInstanceSectionY; instanceSectionY++) {
                    for (int instanceSectionZ = minInstanceSectionZ; instanceSectionZ <= maxInstanceSectionZ; instanceSectionZ++) {
                        // Get the target chunk
                        final Chunk targetChunk = batch.generate() ?
                                loadOptionalChunk(instanceSectionX, instanceSectionZ).join() : getChunk(instanceSectionX, instanceSectionZ);
                        if (targetChunk == null) continue;
                        chunkIndexes.add(chunkIndex(instanceSectionX, instanceSectionZ));
                        synchronized (targetChunk) {
                            final Section targetSection = targetChunk.getSection(instanceSectionY);

                            // Calculate the overlap region
                            final int instanceGlobalX = instanceSectionX * 16;
                            final int instanceGlobalY = instanceSectionY * 16;
                            final int instanceGlobalZ = instanceSectionZ * 16;

                            final int overlapMinX = Math.max(globalSectionX, instanceGlobalX);
                            final int overlapMaxX = Math.min(globalSectionX + 15, instanceGlobalX + 15);
                            final int overlapMinY = Math.max(globalSectionY, instanceGlobalY);
                            final int overlapMaxY = Math.min(globalSectionY + 15, instanceGlobalY + 15);
                            final int overlapMinZ = Math.max(globalSectionZ, instanceGlobalZ);
                            final int overlapMaxZ = Math.min(globalSectionZ + 15, instanceGlobalZ + 15);

                            if (batch.aligned()) {
                                // Use optimized copyFrom with offset for section-aligned batches
                                final int offsetX = overlapMinX - instanceGlobalX;
                                final int offsetY = overlapMinY - instanceGlobalY;
                                final int offsetZ = overlapMinZ - instanceGlobalZ;

                                // Clear NBT data for blocks that will be overwritten in the overlap region
                                for (int dx = 0; dx <= overlapMaxX - overlapMinX; dx++) {
                                    for (int dy = 0; dy <= overlapMaxY - overlapMinY; dy++) {
                                        for (int dz = 0; dz <= overlapMaxZ - overlapMinZ; dz++) {
                                            final int globalX = overlapMinX + dx;
                                            final int globalY = overlapMinY + dy;
                                            final int globalZ = overlapMinZ + dz;
                                            clearBlockNbtData(targetChunk, globalX, globalY, globalZ);
                                        }
                                    }
                                }

                                // Create a temporary palette for the overlap region
                                final Palette tempPalette = Palette.blocks();
                                final int batchOffsetX = overlapMinX - globalSectionX;
                                final int batchOffsetY = overlapMinY - globalSectionY;
                                final int batchOffsetZ = overlapMinZ - globalSectionZ;

                                // Copy the overlapping region from batch palette to temp palette
                                for (int dx = 0; dx <= overlapMaxX - overlapMinX; dx++) {
                                    for (int dy = 0; dy <= overlapMaxY - overlapMinY; dy++) {
                                        for (int dz = 0; dz <= overlapMaxZ - overlapMinZ; dz++) {
                                            final int value = sectionState.palette().get(batchOffsetX + dx, batchOffsetY + dy, batchOffsetZ + dz);
                                            if (value != 0) { // Only copy non-air blocks
                                                tempPalette.set(dx, dy, dz, value);
                                            }
                                        }
                                    }
                                }

                                // Copy from temp palette to target section with offset
                                targetSection.blockPalette().copyFrom(tempPalette, offsetX, offsetY, offsetZ);
                            } else {
                                // Use getAllPresent for non-section-aligned batches
                                sectionState.palette().getAllPresent((localX, localY, localZ, value) -> {
                                    final int globalX = globalSectionX + localX;
                                    final int globalY = globalSectionY + localY;
                                    final int globalZ = globalSectionZ + localZ;

                                    // Check if this block is within the current instance section
                                    if (globalX >= overlapMinX && globalX <= overlapMaxX &&
                                            globalY >= overlapMinY && globalY <= overlapMaxY &&
                                            globalZ >= overlapMinZ && globalZ <= overlapMaxZ) {

                                        final int targetX = globalX - instanceGlobalX;
                                        final int targetY = globalY - instanceGlobalY;
                                        final int targetZ = globalZ - instanceGlobalZ;

                                        // Clear NBT data for this specific block position
                                        clearBlockNbtData(targetChunk, globalX, globalY, globalZ);

                                        // Values are +1 in non-section-aligned batches
                                        targetSection.blockPalette().set(targetX, targetY, targetZ, value - 1);
                                    }
                                });
                            }

                            // Handle block states if present (for blocks with NBT or handlers)
                            if (!batch.ignoreData()) {
                                for (Int2ObjectMap.Entry<Block> blockEntry : sectionState.blockStates().int2ObjectEntrySet()) {
                                    final int blockIndex = blockEntry.getIntKey();
                                    final Block block = blockEntry.getValue();

                                    // Convert section block index back to coordinates
                                    final int localX = sectionBlockIndexGetX(blockIndex);
                                    final int localY = sectionBlockIndexGetY(blockIndex);
                                    final int localZ = sectionBlockIndexGetZ(blockIndex);

                                    final int globalBlockX = globalSectionX + localX;
                                    final int globalBlockY = globalSectionY + localY;
                                    final int globalBlockZ = globalSectionZ + localZ;

                                    // Check if this block is within the current instance section
                                    if (globalBlockX >= overlapMinX && globalBlockX <= overlapMaxX &&
                                            globalBlockY >= overlapMinY && globalBlockY <= overlapMaxY &&
                                            globalBlockZ >= overlapMinZ && globalBlockZ <= overlapMaxZ) {

                                        setBlock(globalBlockX, globalBlockY, globalBlockZ, block);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean placeBlock(@NotNull BlockHandler.Placement placement, boolean doBlockUpdates) {
        final Point blockPosition = placement.getBlockPosition();
        final Chunk chunk = getChunkAt(blockPosition);
        if (!isLoaded(chunk)) return false;
        UNSAFE_setBlock(chunk, blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ(),
                placement.getBlock(), placement, null, doBlockUpdates, 0);
        return true;
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition, @NotNull BlockFace blockFace, boolean doBlockUpdates) {
        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "You cannot break blocks in a null chunk!");
        if (chunk.isReadOnly()) return false;
        if (!isLoaded(chunk)) return false;

        final Block block = getBlock(blockPosition);
        final int x = blockPosition.blockX();
        final int y = blockPosition.blockY();
        final int z = blockPosition.blockZ();
        if (block.isAir()) {
            // The player probably have a wrong version of this chunk section, send it
            chunk.sendChunk(player);
            return false;
        }
        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, block, Block.AIR, new BlockVec(blockPosition), blockFace);
        EventDispatcher.call(blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            final Block resultBlock = blockBreakEvent.getResultBlock();
            UNSAFE_setBlock(chunk, x, y, z, resultBlock, null,
                    new BlockHandler.PlayerDestroy(block, this, blockPosition, player), doBlockUpdates, 0);
            // Send the block break effect packet
            PacketSendingUtils.sendGroupedPacket(chunk.getViewers(),
                    new WorldEventPacket(WorldEvent.PARTICLES_DESTROY_BLOCK.id(), blockPosition, block.stateId(), false),
                    // Prevent the block breaker to play the particles and sound two times
                    (viewer) -> !viewer.equals(player));
        }
        return allowed;
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> retrieveChunk(chunkX, chunkZ));
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        return loadOrRetrieve(chunkX, chunkZ, () -> hasEnabledAutoChunkLoad() ? retrieveChunk(chunkX, chunkZ) : AsyncUtils.empty());
    }

    @Override
    public synchronized void unloadChunk(@NotNull Chunk chunk) {
        if (!isLoaded(chunk)) return;
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();
        chunk.sendPacketToViewers(new UnloadChunkPacket(chunkX, chunkZ));
        EventDispatcher.call(new InstanceChunkUnloadEvent(this, chunk));
        // Remove all entities in chunk
        getEntityTracker().chunkEntities(chunkX, chunkZ, EntityTracker.Target.ENTITIES).forEach(Entity::remove);
        // Clear cache
        this.chunks.remove(chunkIndex(chunkX, chunkZ));
        chunk.unload();
        chunkLoader.unloadChunk(chunk);
        var dispatcher = MinecraftServer.process().dispatcher();
        dispatcher.deletePartition(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(chunkIndex(chunkX, chunkZ));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        final IChunkLoader chunkLoader = this.chunkLoader;
        return optionalAsync(chunkLoader.supportsParallelSaving(), () -> chunkLoader.saveInstance(this));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        final IChunkLoader chunkLoader = this.chunkLoader;
        return optionalAsync(chunkLoader.supportsParallelSaving(), () -> chunkLoader.saveChunk(chunk));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        final IChunkLoader chunkLoader = this.chunkLoader;
        return optionalAsync(chunkLoader.supportsParallelSaving(), () -> chunkLoader.saveChunks(getChunks()));
    }

    private CompletableFuture<Void> optionalAsync(boolean async, Runnable runnable) {
        if (!async) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
        return future;
    }

    protected @NotNull CompletableFuture<@NotNull Chunk> retrieveChunk(int chunkX, int chunkZ) {
        CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
        final long index = chunkIndex(chunkX, chunkZ);
        final CompletableFuture<Chunk> prev = loadingChunks.putIfAbsent(index, completableFuture);
        if (prev != null) return prev;
        final IChunkLoader loader = chunkLoader;
        final Consumer<Chunk> generate = chunk -> {
            if (chunk == null) {
                // Loader couldn't load the chunk, generate it
                chunk = createChunk(chunkX, chunkZ);
                chunk.onGenerate();
            }

            // TODO run in the instance thread?
            cacheChunk(chunk);
            chunk.onLoad();

            EventDispatcher.call(new InstanceChunkLoadEvent(this, chunk));
            final CompletableFuture<Chunk> future = this.loadingChunks.remove(index);
            assert future == completableFuture : "Invalid future: " + future;
            completableFuture.complete(chunk);
        };
        if (loader.supportsParallelLoading()) {
            Thread.startVirtualThread(() -> {
                try {
                    final Chunk chunk = loader.loadChunk(this, chunkX, chunkZ);
                    generate.accept(chunk);
                } catch (Throwable e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            });
        } else {
            final Chunk chunk = loader.loadChunk(this, chunkX, chunkZ);
            Thread.startVirtualThread(() -> {
                try {
                    generate.accept(chunk);
                } catch (Throwable e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            });
        }
        return completableFuture;
    }

    Map<Long, List<GeneratorImpl.SectionModifierImpl>> generationForks = new ConcurrentHashMap<>();

    protected @NotNull Chunk createChunk(int chunkX, int chunkZ) {
        final Chunk chunk = chunkSupplier.createChunk(this, chunkX, chunkZ);
        Check.notNull(chunk, "Chunks supplied by a ChunkSupplier cannot be null.");
        Generator generator = generator();
        if (generator == null || !chunk.shouldGenerate()) {
            // No chunk generator, execute the callback with the empty chunk
            processFork(chunk);
            return chunk;
        }
        GeneratorImpl.GenSection[] genSections = new GeneratorImpl.GenSection[chunk.getSections().size()];
        Arrays.setAll(genSections, i -> {
            Section section = chunk.getSections().get(i);
            return new GeneratorImpl.GenSection(section.blockPalette(), section.biomePalette());
        });
        var chunkUnit = GeneratorImpl.chunk(MinecraftServer.getBiomeRegistry(), genSections,
                chunk.getChunkX(), chunk.minSection, chunk.getChunkZ());
        try {
            // Generate block/biome palette
            generator.generate(chunkUnit);
            // Apply nbt/handler
            if (chunkUnit.modifier() instanceof GeneratorImpl.AreaModifierImpl chunkModifier) {
                for (var section : chunkModifier.sections()) {
                    if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                        applyGenerationData(chunk, sectionModifier);
                    }
                }
            }
            // Register forks or apply locally
            for (var fork : chunkUnit.forks()) {
                var sections = ((GeneratorImpl.AreaModifierImpl) fork.modifier()).sections();
                for (var section : sections) {
                    if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                        if (sectionModifier.genSection().blocks().count() == 0)
                            continue;
                        final Point start = section.absoluteStart();
                        final Chunk forkChunk = start.chunkX() == chunkX && start.chunkZ() == chunkZ ? chunk : getChunkAt(start);
                        if (forkChunk != null) {
                            applyFork(forkChunk, sectionModifier);
                            // Update players
                            forkChunk.invalidate();
                            forkChunk.sendChunk();
                        } else {
                            final long index = chunkIndex(start);
                            this.generationForks.compute(index, (i, sectionModifiers) -> {
                                if (sectionModifiers == null) sectionModifiers = new ArrayList<>();
                                sectionModifiers.add(sectionModifier);
                                return sectionModifiers;
                            });
                        }
                    }
                }
            }
            // Apply awaiting forks
            processFork(chunk);
        } catch (Throwable e) {
            MinecraftServer.getExceptionManager().handleException(e);
        } finally {
            // End generation
            refreshLastBlockChangeTime();
        }
        return chunk;
    }

    private void processFork(Chunk chunk) {
        this.generationForks.compute(chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), (aLong, sectionModifiers) -> {
            if (sectionModifiers != null) {
                for (var sectionModifier : sectionModifiers) {
                    applyFork(chunk, sectionModifier);
                }
            }
            return null;
        });
    }

    private void applyFork(Chunk chunk, GeneratorImpl.SectionModifierImpl sectionModifier) {
        synchronized (chunk) {
            Section section = chunk.getSectionAt(sectionModifier.start().blockY());
            Palette currentBlocks = section.blockPalette();
            // -1 is necessary because forked units handle explicit changes by changing AIR 0 to 1
            sectionModifier.genSection().blocks().getAllPresent((x, y, z, value) -> currentBlocks.set(x, y, z, value - 1));
            applyGenerationData(chunk, sectionModifier);
        }
    }

    private void applyGenerationData(Chunk chunk, GeneratorImpl.SectionModifierImpl section) {
        var cache = section.genSection().specials();
        if (cache.isEmpty()) return;
        final int height = section.start().blockY();
        synchronized (chunk) {
            Int2ObjectMaps.fastForEach(cache, blockEntry -> {
                final int index = blockEntry.getIntKey();
                final Block block = blockEntry.getValue();
                final int x = chunkBlockIndexGetX(index);
                final int y = chunkBlockIndexGetY(index) + height;
                final int z = chunkBlockIndexGetZ(index);
                chunk.setBlock(x, y, z, block);
            });
        }
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        this.autoChunkLoad = enable;
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return autoChunkLoad;
    }

    @Override
    public boolean isInVoid(@NotNull Point point) {
        // TODO: more customizable
        return point.y() < getCachedDimensionType().minY() - 64;
    }

    /**
     * Changes which type of {@link Chunk} implementation to use once one needs to be loaded.
     * <p>
     * Uses {@link DynamicChunk} by default.
     * <p>
     * WARNING: if you need to save this instance's chunks later,
     * the code needs to be predictable for {@link IChunkLoader#loadChunk(Instance, int, int)}
     * to create the correct type of {@link Chunk}. tl;dr: Need chunk save = no random type.
     *
     * @param chunkSupplier the new {@link ChunkSupplier} of this instance, chunks need to be non-null
     * @throws NullPointerException if {@code chunkSupplier} is null
     */
    @Override
    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.chunkSupplier = chunkSupplier;
    }

    /**
     * Gets the current {@link ChunkSupplier}.
     * <p>
     * You shouldn't use it to generate a new chunk, but as a way to view which one is currently in use.
     *
     * @return the current {@link ChunkSupplier}
     */
    public ChunkSupplier getChunkSupplier() {
        return chunkSupplier;
    }

    /**
     * Gets all the {@link SharedInstance} linked to this container.
     *
     * @return an unmodifiable {@link List} containing all the {@link SharedInstance} linked to this container
     */
    public List<SharedInstance> getSharedInstances() {
        return Collections.unmodifiableList(sharedInstances);
    }

    /**
     * Gets if this instance has {@link SharedInstance} linked to it.
     *
     * @return true if {@link #getSharedInstances()} is not empty
     */
    public boolean hasSharedInstances() {
        return !sharedInstances.isEmpty();
    }

    /**
     * Assigns a {@link SharedInstance} to this container.
     * <p>
     * Only used by {@link InstanceManager}, mostly unsafe.
     *
     * @param sharedInstance the shared instance to assign to this container
     */
    protected void addSharedInstance(SharedInstance sharedInstance) {
        this.sharedInstances.add(sharedInstance);
    }

    /**
     * Copies all the chunks of this instance and create a new instance container with all of them.
     * <p>
     * Chunks are copied with {@link Chunk#copy(Instance, int, int)},
     * {@link UUID} is randomized and {@link DimensionType} is passed over.
     *
     * @return an {@link InstanceContainer} with the exact same chunks as 'this'
     * @see #getSrcInstance() to retrieve the "creation source" of the copied instance
     */
    public synchronized InstanceContainer copy() {
        InstanceContainer copiedInstance = new InstanceContainer(UUID.randomUUID(), getDimensionType());
        copiedInstance.srcInstance = this;
        copiedInstance.tagHandler = this.tagHandler.copy();
        copiedInstance.lastBlockChangeTime = this.lastBlockChangeTime;
        for (Chunk chunk : chunks.values()) {
            final int chunkX = chunk.getChunkX();
            final int chunkZ = chunk.getChunkZ();
            final Chunk copiedChunk = chunk.copy(copiedInstance, chunkX, chunkZ);
            copiedInstance.cacheChunk(copiedChunk);
        }
        return copiedInstance;
    }

    /**
     * Gets the instance from which this one has been copied.
     * <p>
     * Only present if this instance has been created with {@link InstanceContainer#copy()}.
     *
     * @return the instance source, null if not created by a copy
     * @see #copy() to create a copy of this instance with 'this' as the source
     */
    public @Nullable InstanceContainer getSrcInstance() {
        return srcInstance;
    }

    /**
     * Gets the last time at which a block changed.
     *
     * @return the time at which the last block changed in nanoseconds. Only use this to calculate delta times
     */
    public long getLastBlockChangeTime() {
        return lastBlockChangeTime;
    }

    /**
     * Signals the instance that a block changed.
     * <p>
     * Useful if you change blocks values directly using a {@link Chunk} object.
     */
    public void refreshLastBlockChangeTime() {
        this.lastBlockChangeTime = System.nanoTime();
    }

    @Override
    public @Nullable Generator generator() {
        return generator;
    }

    @Override
    public void setGenerator(@Nullable Generator generator) {
        this.generator = generator;
    }

    /**
     * Gets all the instance chunks.
     *
     * @return the chunks of this instance
     */
    @Override
    public @NotNull Collection<@NotNull Chunk> getChunks() {
        return chunks.values();
    }

    /**
     * Gets the {@link IChunkLoader} of this instance.
     *
     * @return the {@link IChunkLoader} of this instance
     */
    public @NotNull IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    /**
     * Changes the {@link IChunkLoader} of this instance (to change how chunks are retrieved when not already loaded).
     *
     * <p>{@link IChunkLoader#noop()} can be used to do nothing.</p>
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    public void setChunkLoader(@NotNull IChunkLoader chunkLoader) {
        this.chunkLoader = Objects.requireNonNull(chunkLoader, "Chunk loader cannot be null");
    }

    @Override
    public void tick(long time) {
        // Time/world border
        super.tick(time);
        // Clear block change map
        Lock wrlock = this.changingBlockLock;
        wrlock.lock();
        this.currentlyChangingBlocks.clear();
        wrlock.unlock();
    }

    /**
     * Has this block already changed since last update?
     * Prevents StackOverflow with blocks trying to modify their position in onDestroy or onPlace.
     *
     * @param blockPosition the block position
     * @param block         the block
     * @return true if the block changed since the last update
     */
    private boolean isAlreadyChanged(@NotNull BlockVec blockPosition, @NotNull Block block) {
        final Block changedBlock = currentlyChangingBlocks.get(blockPosition);
        return Objects.equals(changedBlock, block);
    }

    /**
     * Executed when a block is modified, this is used to modify the states of neighbours blocks.
     * <p>
     * For example, this can be used for redstone wires which need an understanding of its neighborhoods to take the right shape.
     *
     * @param blockPosition the position of the modified block
     */
    private void executeNeighboursBlockPlacementRule(@NotNull Point blockPosition, int updateDistance) {
        ChunkCache cache = new ChunkCache(this, null, null);
        for (var updateFace : BLOCK_UPDATE_FACES) {
            var direction = updateFace.toDirection();
            final int neighborX = blockPosition.blockX() + direction.normalX();
            final int neighborY = blockPosition.blockY() + direction.normalY();
            final int neighborZ = blockPosition.blockZ() + direction.normalZ();
            if (neighborY < getCachedDimensionType().minY() || neighborY > getCachedDimensionType().height())
                continue;
            final Block neighborBlock = cache.getBlock(neighborX, neighborY, neighborZ, Condition.NONE);
            if (neighborBlock == null || neighborBlock.isAir())
                continue;
            final BlockPlacementRule neighborBlockPlacementRule = MinecraftServer.getBlockManager().getBlockPlacementRule(neighborBlock);
            if (neighborBlockPlacementRule == null || updateDistance >= neighborBlockPlacementRule.maxUpdateDistance())
                continue;

            final Vec neighborPosition = new Vec(neighborX, neighborY, neighborZ);
            final Block newNeighborBlock = neighborBlockPlacementRule.blockUpdate(new BlockPlacementRule.UpdateState(
                    this,
                    neighborPosition,
                    neighborBlock,
                    updateFace.getOppositeFace()
            ));
            if (neighborBlock != newNeighborBlock) {
                final Chunk chunk = getChunkAt(neighborPosition);
                if (!isLoaded(chunk)) continue;
                UNSAFE_setBlock(chunk, neighborPosition.blockX(), neighborPosition.blockY(), neighborPosition.blockZ(), newNeighborBlock,
                        null, null, true, updateDistance + 1);
            }
        }
    }

    private CompletableFuture<Chunk> loadOrRetrieve(int chunkX, int chunkZ, Supplier<CompletableFuture<Chunk>> supplier) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            return CompletableFuture.completedFuture(chunk);
        }
        return supplier.get();
    }

    private void cacheChunk(@NotNull Chunk chunk) {
        this.chunks.put(chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), chunk);
        var dispatcher = MinecraftServer.process().dispatcher();
        dispatcher.createPartition(chunk);
    }

    /**
     * Clears NBT data for all blocks in a section.
     * This is called when an entire section is being replaced by a batch operation.
     */
    private void clearSectionNbtData(@NotNull Chunk chunk, int sectionX, int sectionY, int sectionZ) {
        // Clear NBT data for all blocks in this section
        if (chunk instanceof DynamicChunk dynamicChunk && dynamicChunk.entries.isEmpty()) return;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    final int globalX = sectionX * 16 + x;
                    final int globalY = sectionY * 16 + y;
                    final int globalZ = sectionZ * 16 + z;
                    clearBlockNbtData(chunk, globalX, globalY, globalZ);
                }
            }
        }
    }

    /**
     * Clears NBT data for a specific block position.
     * This ensures that when a block is overwritten, any existing NBT data is properly removed.
     */
    private void clearBlockNbtData(@NotNull Chunk chunk, int x, int y, int z) {
        final Block currentBlock = chunk.getBlock(x, y, z);
        // If the current block has data, we need to clear it
        if (currentBlock.hasNbt() || currentBlock.handler() != null) {
            chunk.setBlock(x, y, z, Block.AIR);
        }
    }
}
