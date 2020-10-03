package net.minestom.server.instance;

import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockProvider;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.block.CustomBlockUtils;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

/**
 * InstanceContainer is an instance that contains chunks in contrary to SharedInstance.
 */
public class InstanceContainer extends Instance {

    private static final String UUID_KEY = "uuid";
    private static final String DATA_KEY = "data";

    private StorageLocation storageLocation;

    private List<SharedInstance> sharedInstances = new CopyOnWriteArrayList<>();

    private ChunkGenerator chunkGenerator;
    private final ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<>();
    protected final Set<Chunk> scheduledChunksToRemove = new HashSet<>();

    private ReadWriteLock changingBlockLock = new ReentrantReadWriteLock();
    private Map<BlockPosition, Block> currentlyChangingBlocks = new HashMap<>();
    private IChunkLoader chunkLoader;

    private boolean autoChunkLoad;

    @Setter
    private BiFunction<Integer, Integer, BlockProvider> chunkDecider;

    /**
     * Create an {@link InstanceContainer}
     *
     * @param uniqueId        the unique id of the instance
     * @param dimensionType   the dimension type of the instance
     * @param storageLocation the {@link StorageLocation} of the instance,
     *                        can be null if you do not wish to save the instance later on
     */
    public InstanceContainer(UUID uniqueId, DimensionType dimensionType, StorageLocation storageLocation) {
        super(uniqueId, dimensionType);

        this.storageLocation = storageLocation;
        this.chunkLoader = new MinestomBasicChunkLoader(storageLocation);

        // Get instance data from the saved data if a StorageLocation is defined
        if (storageLocation != null) {
            // Retrieve instance data
            this.uniqueId = storageLocation.getOrDefault(UUID_KEY, UUID.class, uniqueId);

            final Data data = storageLocation.getOrDefault(DATA_KEY, SerializableData.class, null);
            setData(data);
        }
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, Data data) {
        setBlock(x, y, z, blockStateId, null, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setBlock(x, y, z, customBlock.getDefaultBlockStateId(), customBlock, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setBlock(x, y, z, blockStateId, customBlock, data);
    }

    /**
     * Set a block at the position
     * <p>
     * Verifies if the {@link Chunk} at the position is loaded, place the block if yes.
     * Otherwise, check if {@link #hasEnabledAutoChunkLoad()} is true to load the chunk automatically and place the block.
     *
     * @param x            the block X
     * @param y            the block Y
     * @param z            the block Z
     * @param blockStateId the block state id
     * @param customBlock  the {@link CustomBlock}, null if none
     * @param data         the {@link Data}, null if none
     */
    private synchronized void setBlock(int x, int y, int z, short blockStateId, CustomBlock customBlock, Data data) {
        final Chunk chunk = getChunkAt(x, z);
        if (ChunkUtils.isLoaded(chunk)) {
            UNSAFE_setBlock(chunk, x, y, z, blockStateId, customBlock, data);
        } else {
            if (hasEnabledAutoChunkLoad()) {
                final int chunkX = ChunkUtils.getChunkCoordinate((int) x);
                final int chunkZ = ChunkUtils.getChunkCoordinate((int) z);
                loadChunk(chunkX, chunkZ, c -> {
                    UNSAFE_setBlock(c, x, y, z, blockStateId, customBlock, data);
                });
            } else {
                throw new IllegalStateException("Tried to set a block to an unloaded chunk with auto chunk load disabled");
            }
        }
    }

    /**
     * Set a block at the position
     * <p>
     * Unsafe because the method is not synchronized and it does not verify if the chunk is loaded or not
     *
     * @param chunk        the {@link Chunk} which should be loaded
     * @param x            the block X
     * @param y            the block Y
     * @param z            the block Z
     * @param blockStateId the block state id
     * @param customBlock  the {@link CustomBlock}, null if none
     * @param data         the {@link Data}, null if none
     */
    private void UNSAFE_setBlock(Chunk chunk, int x, int y, int z, short blockStateId, CustomBlock customBlock, Data data) {
        synchronized (chunk) {

            final boolean isCustomBlock = customBlock != null;

            final BlockPosition blockPosition = new BlockPosition(x, y, z);

            if (isAlreadyChanged(blockPosition, blockStateId)) { // do NOT change the block again.
                // Avoids StackOverflowExceptions when onDestroy tries to destroy the block itself
                // This can happen with nether portals which break the entire frame when a portal block is broken
                return;
            }
            setAlreadyChanged(blockPosition, blockStateId);
            final int index = ChunkUtils.getBlockIndex(x, y, z);

            final CustomBlock previousBlock = chunk.getCustomBlock(index);
            if (previousBlock != null) {
                // Previous block was a custom block

                // Call the destroy listener
                callBlockDestroy(chunk, index, previousBlock, blockPosition);

                // Remove digging information for the previous custom block
                previousBlock.removeDiggingInformation(this, blockPosition);
            }

            // Change id based on neighbors
            blockStateId = executeBlockPlacementRule(blockStateId, blockPosition);

            // Retrieve custom block values
            short customBlockId = 0;
            boolean hasUpdate = false;
            if (isCustomBlock) {
                customBlockId = customBlock.getCustomBlockId();
                data = customBlock.createData(this, blockPosition, data);
                hasUpdate = CustomBlockUtils.hasUpdate(customBlock);
            }

            // Set the block
            chunk.setBlock(x, y, z, blockStateId, customBlockId, data, hasUpdate);

            // Refresh neighbors since a new block has been placed
            executeNeighboursBlockPlacementRule(blockPosition);

            // Refresh player chunk block
            sendBlockChange(chunk, blockPosition, blockStateId);

            // Call the place listener for custom block
            if (isCustomBlock)
                callBlockPlace(chunk, index, blockPosition);
        }
    }

    private void setAlreadyChanged(BlockPosition blockPosition, short blockStateId) {
        currentlyChangingBlocks.put(blockPosition, Block.fromStateId(blockStateId));
    }

    /**
     * Has this block already changed since last update? Prevents StackOverflow with blocks trying to modify their position in onDestroy or onPlace
     *
     * @param blockPosition the block position
     * @param blockStateId  the block state id
     * @return
     */
    private boolean isAlreadyChanged(BlockPosition blockPosition, short blockStateId) {
        final Block changedBlock = currentlyChangingBlocks.get(blockPosition);
        if (changedBlock == null)
            return false;
        return changedBlock.getBlockId() == blockStateId;
    }

    @Override
    public void refreshBlockStateId(BlockPosition blockPosition, short blockStateId) {
        final Chunk chunk = getChunkAt(blockPosition.getX(), blockPosition.getZ());
        synchronized (chunk) {
            chunk.refreshBlockStateId(blockPosition.getX(), blockPosition.getY(),
                    blockPosition.getZ(), blockStateId);

            sendBlockChange(chunk, blockPosition, blockStateId);
        }
    }

    /**
     * Call {@link CustomBlock#onDestroy(Instance, BlockPosition, Data)} for {@code previousBlock}
     * <p>
     * WARNING {@code chunk} needs to be synchronized
     *
     * @param chunk         the chunk where the block is
     * @param index         the index of the block
     * @param previousBlock the block which has been destroyed
     * @param blockPosition the block position
     */
    private void callBlockDestroy(Chunk chunk, int index, CustomBlock previousBlock, BlockPosition blockPosition) {
        final Data previousData = chunk.getBlockData(index);
        previousBlock.onDestroy(this, blockPosition, previousData);
    }

    /**
     * Call {@link CustomBlock#onPlace(Instance, BlockPosition, Data)} for the current custom block at the position
     * <p>
     * WARNING {@code chunk} needs to be synchronized
     *
     * @param chunk         the chunk where the block is
     * @param index         the index of the block
     * @param blockPosition the block position
     */
    private void callBlockPlace(Chunk chunk, int index, BlockPosition blockPosition) {
        final CustomBlock actualBlock = chunk.getCustomBlock(index);
        if (actualBlock == null)
            return;
        final Data previousData = chunk.getBlockData(index);
        actualBlock.onPlace(this, blockPosition, previousData);
    }

    /**
     * Call the {@link BlockPlacementRule} for the specified block state id
     *
     * @param blockStateId  the block state id to modify
     * @param blockPosition the block position
     * @return the modified block state id
     */
    private short executeBlockPlacementRule(short blockStateId, BlockPosition blockPosition) {
        final BlockPlacementRule blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(blockStateId);
        if (blockPlacementRule != null) {
            return blockPlacementRule.blockRefresh(this, blockPosition, blockStateId);
        }
        return blockStateId;
    }

    /**
     * Executed when a block is modified, this is used to modify the states of neighbours blocks
     * <p>
     * For example, this can be used for redstone wires which need an understanding of its neighborhoods to take the right shape
     *
     * @param blockPosition the position of the modified block
     */
    private void executeNeighboursBlockPlacementRule(BlockPosition blockPosition) {
        for (int offsetX = -1; offsetX < 2; offsetX++) {
            for (int offsetY = -1; offsetY < 2; offsetY++) {
                for (int offsetZ = -1; offsetZ < 2; offsetZ++) {
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0)
                        continue;
                    final int neighborX = blockPosition.getX() + offsetX;
                    final int neighborY = blockPosition.getY() + offsetY;
                    final int neighborZ = blockPosition.getZ() + offsetZ;
                    final Chunk chunk = getChunkAt(neighborX, neighborZ);

                    // Do not try to get neighbour in an unloaded chunk
                    if (chunk == null)
                        continue;

                    final short neighborStateId = chunk.getBlockStateId(neighborX, neighborY, neighborZ);
                    final BlockPlacementRule neighborBlockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(neighborStateId);
                    if (neighborBlockPlacementRule != null) {
                        final short newNeighborId = neighborBlockPlacementRule.blockRefresh(this,
                                new BlockPosition(neighborX, neighborY, neighborZ), neighborStateId);
                        if (neighborStateId != newNeighborId) {
                            refreshBlockStateId(neighborX, neighborY, neighborZ, neighborStateId);
                        }
                    }

                    // Update neighbors
                    final CustomBlock customBlock = getCustomBlock(neighborX, neighborY, neighborZ);
                    if (customBlock != null) {
                        boolean directNeighbor = false; // only if directly connected to neighbor (no diagonals)
                        if (offsetX != 0 ^ offsetZ != 0) {
                            directNeighbor = offsetY == 0;
                        } else if (offsetX == 0 && offsetZ == 0) {
                            directNeighbor = true;
                        }
                        customBlock.updateFromNeighbor(this, new BlockPosition(neighborX, neighborY, neighborZ), blockPosition, directNeighbor);
                    }
                }
            }
        }
    }

    @Override
    public boolean breakBlock(Player player, BlockPosition blockPosition) {
        player.resetTargetBlock();

        final Chunk chunk = getChunkAt(blockPosition);

        // Chunk unloaded, stop here
        if (!ChunkUtils.isLoaded(chunk))
            return false;

        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        final short blockStateId = getBlockStateId(x, y, z);

        // The player probably have a wrong version of this chunk section, send it
        if (blockStateId == 0) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(y), player);
            return false;
        }

        final CustomBlock customBlock = getCustomBlock(x, y, z);

        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, blockPosition, blockStateId, customBlock, (short) 0, (short) 0);
        player.callEvent(PlayerBlockBreakEvent.class, blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            setSeparateBlocks(x, y, z, blockBreakEvent.getResultBlockStateId(), blockBreakEvent.getResultCustomBlockId());

            ParticlePacket particlePacket = ParticleCreator.createParticlePacket(Particle.BLOCK, false,
                    x + 0.5f, y, z + 0.5f,
                    0.4f, 0.5f, 0.4f,
                    0.3f, 125, writer -> {
                        writer.writeVarInt(blockStateId);
                    });

            chunk.getViewers().forEach(p -> {
                // The player who breaks the block already get particles client-side
                if (customBlock != null || !(p.equals(player) && !player.isCreative())) {
                    p.getPlayerConnection().sendPacket(particlePacket);
                }
            });

        }

        return allowed;
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            if (callback != null)
                callback.accept(chunk);
        } else {
            // Retrieve chunk from somewhere else (file or create a new one using the ChunkGenerator)
            retrieveChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            if (callback != null)
                callback.accept(chunk);
        } else {
            if (hasEnabledAutoChunkLoad()) {
                // Load chunk from StorageLocation or with ChunkGenerator
                retrieveChunk(chunkX, chunkZ, callback);
            } else {
                // Chunk not loaded, return null
                if (callback != null)
                    callback.accept(null);
            }
        }
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        // Already unloaded chunk
        if (!ChunkUtils.isLoaded(chunk)) {
            return;
        }
        // Schedule the chunk removal
        synchronized (this.scheduledChunksToRemove) {
            this.scheduledChunksToRemove.add(chunk);
        }
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        final Chunk chunk = chunks.get(index);
        return ChunkUtils.isLoaded(chunk) ? chunk : null;
    }

    /**
     * Save the instance ({@link #getUniqueId()} {@link #getData()}) and call {@link #saveChunksToStorage(Runnable)}
     * <p>
     * WARNING: {@link #getData()} needs to be a {@link SerializableData} in order to be saved
     *
     * @param callback the callback once the saving is done
     */
    public void saveInstance(Runnable callback) {
        Check.notNull(getStorageLocation(), "You cannot save the instance if no StorageLocation has been defined");

        this.storageLocation.set(UUID_KEY, getUniqueId(), UUID.class);
        final Data data = getData();
        if (data != null) {
            // Save the instance data
            Check.stateCondition(!(data instanceof SerializableData),
                    "Instance#getData needs to be a SerializableData in order to be saved");
            this.storageLocation.set(DATA_KEY, (SerializableData) getData(), SerializableData.class);
        }

        saveChunksToStorage(callback);
    }

    /**
     * Save the instance without callback
     *
     * @see #saveInstance(Runnable)
     */
    public void saveInstance() {
        saveInstance(null);
    }

    @Override
    public void saveChunkToStorage(Chunk chunk, Runnable callback) {
        chunkLoader.saveChunk(chunk, callback);
    }

    @Override
    public void saveChunksToStorage(Runnable callback) {
        if (chunkLoader.supportsParallelSaving()) {
            ExecutorService parallelSavingThreadPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PARALLEL_CHUNK_SAVING, MinecraftServer.THREAD_NAME_PARALLEL_CHUNK_SAVING, true);
            getChunks().forEach(c -> parallelSavingThreadPool.execute(() -> {
                saveChunkToStorage(c, null);
            }));
            try {
                parallelSavingThreadPool.shutdown();
                parallelSavingThreadPool.awaitTermination(1L, java.util.concurrent.TimeUnit.DAYS);
                if (callback != null)
                    callback.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            final Iterator<Chunk> chunkIterator = chunks.values().iterator();
            while (chunkIterator.hasNext()) {
                final Chunk chunk = chunkIterator.next();
                final boolean isLast = !chunkIterator.hasNext();
                saveChunkToStorage(chunk, isLast ? callback : null);
            }
        }
    }

    @Override
    public BlockBatch createBlockBatch() {
        return new BlockBatch(this);
    }

    @Override
    public ChunkBatch createChunkBatch(Chunk chunk) {
        Check.notNull(chunk, "The chunk of a ChunkBatch cannot be null");
        return new ChunkBatch(this, chunk);
    }

    @Override
    protected void retrieveChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        final boolean loaded = chunkLoader.loadChunk(this, chunkX, chunkZ, chunk -> {
            cacheChunk(chunk);
            callChunkLoadEvent(chunkX, chunkZ);
            UPDATE_MANAGER.signalChunkLoad(this, chunkX, chunkZ);
            if (callback != null)
                callback.accept(chunk);
        });

        if (!loaded) {
            // Not found, create a new chunk
            createChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    protected void createChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        Biome[] biomes = new Biome[Chunk.BIOME_COUNT];
        if (chunkGenerator == null) {
            Arrays.fill(biomes, MinecraftServer.getBiomeManager().getById(0));
        } else {
            chunkGenerator.fillBiomes(biomes, chunkX, chunkZ);
        }

        final Chunk chunk;
        final BlockProvider blockProvider = chunkDecider != null ? chunkDecider.apply(chunkX, chunkZ) : null;
        if (blockProvider != null) {
            // Use static chunk
            chunk = new StaticChunk(this, biomes, chunkX, chunkZ, blockProvider);
        } else {
            // Use dynamic chunk
            chunk = new DynamicChunk(this, biomes, chunkX, chunkZ);
        }

        cacheChunk(chunk);

        if (chunkGenerator != null && blockProvider == null) {
            // Execute the chunk generator to populate the chunk
            final ChunkBatch chunkBatch = createChunkBatch(chunk);

            chunkBatch.flushChunkGenerator(chunkGenerator, callback);
        } else {
            // No chunk generator, execute the callback with the empty chunk
            if (callback != null)
                callback.accept(chunk);
        }

        UPDATE_MANAGER.signalChunkLoad(this, chunkX, chunkZ);
        callChunkLoadEvent(chunkX, chunkZ);
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
    public boolean isInVoid(Position position) {
        // TODO: customizable
        return position.getY() < -64;
    }

    /**
     * Get all the {@link SharedInstance} linked to this container
     *
     * @return an unmodifiable {@link List} containing all the {@link SharedInstance} linked to this container
     */
    public List<SharedInstance> getSharedInstances() {
        return Collections.unmodifiableList(sharedInstances);
    }

    /**
     * Assign a {@link SharedInstance} to this container
     * <p>
     * Only used by {@link InstanceManager}
     *
     * @param sharedInstance the shared instance to assign to this container
     */
    protected void addSharedInstance(SharedInstance sharedInstance) {
        this.sharedInstances.add(sharedInstance);
    }

    /**
     * Add a {@link Chunk} to the internal instance map
     *
     * @param chunk the chunk to cache
     */
    private void cacheChunk(Chunk chunk) {
        final long index = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        this.chunks.put(index, chunk);
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    /**
     * Get all the instance chunks
     *
     * @return the chunks of this instance
     */
    public Collection<Chunk> getChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }

    @Override
    public StorageLocation getStorageLocation() {
        return storageLocation;
    }

    @Override
    public void setStorageLocation(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * Get the {@link IChunkLoader} of this instance
     *
     * @return the {@link IChunkLoader} of this instance
     */
    public IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    /**
     * Change the {@link IChunkLoader} of this instance (to change how chunks are retrieved when not already loaded)
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    public void setChunkLoader(IChunkLoader chunkLoader) {
        this.chunkLoader = chunkLoader;
    }

    /**
     * Send a {@link BlockChangePacket} at the specified {@link BlockPosition} to set the block as {@code blockStateId}
     * <p>
     * WARNING: this does not change the internal block data, this is strictly visual for the players
     *
     * @param chunk         the chunk where the block is
     * @param blockPosition the block position
     * @param blockStateId  the new state of the block
     */
    private void sendBlockChange(Chunk chunk, BlockPosition blockPosition, short blockStateId) {
        BlockChangePacket blockChangePacket = new BlockChangePacket();
        blockChangePacket.blockPosition = blockPosition;
        blockChangePacket.blockStateId = blockStateId;
        chunk.sendPacketToViewers(blockChangePacket);
    }

    @Override
    public void scheduleUpdate(int time, TimeUnit unit, BlockPosition position) {
        final CustomBlock toUpdate = getCustomBlock(position);
        if (toUpdate == null) {
            return;
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            final CustomBlock currentBlock = getCustomBlock(position);
            if (currentBlock == null)
                return;
            if (currentBlock.getCustomBlockId() != toUpdate.getCustomBlockId()) { // block changed
                return;
            }
            currentBlock.scheduledUpdate(this, position, getBlockData(position));
        }).delay(time, unit).schedule();
    }

    @Override
    public void tick(long time) {
        // Unload all waiting chunks
        UNSAFE_unloadChunks();

        // Time/world border
        super.tick(time);

        Lock wrlock = changingBlockLock.writeLock();
        wrlock.lock();
        currentlyChangingBlocks.clear();
        wrlock.unlock();
    }

    /**
     * Unload all waiting chunks
     * <p>
     * Unsafe because it has to be done on the same thread as the instance/chunks tick update
     */
    protected void UNSAFE_unloadChunks() {
        synchronized (this.scheduledChunksToRemove) {
            for (Chunk chunk : scheduledChunksToRemove) {
                final int chunkX = chunk.getChunkX();
                final int chunkZ = chunk.getChunkZ();

                final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);

                UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket();
                unloadChunkPacket.chunkX = chunkX;
                unloadChunkPacket.chunkZ = chunkZ;
                chunk.sendPacketToViewers(unloadChunkPacket);

                for (Player viewer : chunk.getViewers()) {
                    chunk.removeViewer(viewer);
                }

                callChunkUnloadEvent(chunkX, chunkZ);

                // Remove all entities in chunk
                getChunkEntities(chunk).forEach(entity -> {
                    if (!(entity instanceof Player))
                        entity.remove();
                });

                // Clear cache
                this.chunks.remove(index);
                this.chunkEntities.remove(index);

                chunk.unload();

                UPDATE_MANAGER.signalChunkUnload(this, chunkX, chunkZ);
            }
            this.scheduledChunksToRemove.clear();
        }
    }

    private void callChunkLoadEvent(int chunkX, int chunkZ) {
        InstanceChunkLoadEvent chunkLoadEvent = new InstanceChunkLoadEvent(this, chunkX, chunkZ);
        callEvent(InstanceChunkLoadEvent.class, chunkLoadEvent);
    }

    private void callChunkUnloadEvent(int chunkX, int chunkZ) {
        InstanceChunkUnloadEvent chunkUnloadEvent = new InstanceChunkUnloadEvent(this, chunkX, chunkZ);
        callEvent(InstanceChunkUnloadEvent.class, chunkUnloadEvent);
    }
}