package net.minestom.server.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.Player;
import net.minestom.server.event.world.WorldChunkLoadEvent;
import net.minestom.server.event.world.WorldChunkUnloadEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.world.batch.ChunkGenerationBatch;
import net.minestom.server.block.Block;
import net.minestom.server.block.BlockHandler;
import net.minestom.server.block.rule.BlockPlacementRule;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * WorldContainer is an world that contains chunks in contrary to SharedWorld.
 */
public class WorldContainer extends World {

    private static final String UUID_KEY = "uuid";
    private static final String DATA_KEY = "data";

    // the storage location of this world, can be null
    private StorageLocation storageLocation;

    // the shared worlds assigned to this world
    private final List<SharedWorld> sharedWorlds = new CopyOnWriteArrayList<>();

    // the chunk generator used, can be null
    private ChunkGenerator chunkGenerator;
    // (chunk index -> chunk) map, contains all the chunks in the world
    private final Map<Long, Chunk> chunks = new ConcurrentHashMap<>();
    // contains all the chunks to remove during the next world tick, should be synchronized
    protected final Set<Chunk> scheduledChunksToRemove = new HashSet<>();

    private final ReadWriteLock changingBlockLock = new ReentrantReadWriteLock();
    private final Map<BlockPosition, Block> currentlyChangingBlocks = new HashMap<>();

    // the chunk loader, used when trying to load/save a chunk from another source
    private IChunkLoader chunkLoader;

    // used to automatically enable the chunk loading or not
    private boolean autoChunkLoad = true;

    // used to supply a new chunk object at a position when requested
    private ChunkSupplier chunkSupplier;

    // Fields for world copy
    protected WorldContainer srcWorld; // only present if this world has been created using a copy
    private long lastBlockChangeTime; // Time at which the last block change happened (#setBlock)

    /**
     * Creates an {@link WorldContainer}.
     *
     * @param uniqueId        the unique id of the World
     * @param dimensionType   the dimension type of the World
     * @param storageLocation the {@link StorageLocation} of the World,
     *                        can be null if you do not wish to save the World later on
     */
    public WorldContainer(@NotNull UUID uniqueId, @NotNull DimensionType dimensionType, @Nullable StorageLocation storageLocation) {
        super(uniqueId, dimensionType);

        this.storageLocation = storageLocation;

        // Set the default chunk supplier using DynamicChunk
        setChunkSupplier(DynamicChunk::new);

        // Set the default chunk loader which use the world's StorageLocation and ChunkSupplier to save and load chunks
        setChunkLoader(new MinestomBasicChunkLoader(this));

        // Get world data from the saved data if a StorageLocation is defined
        if (storageLocation != null) {
            // Retrieve world data
            this.uniqueId = storageLocation.getOrDefault(UUID_KEY, UUID.class, uniqueId);

            final Data data = storageLocation.getOrDefault(DATA_KEY, SerializableData.class, null);
            setData(data);
        }
    }

    @Override
    public synchronized void setBlock(int x, int y, int z, @NotNull Block block) {
        final Chunk chunk = getChunkAt(x, z);
        if (ChunkUtils.isLoaded(chunk)) {
            UNSAFE_setBlock(chunk, x, y, z, block);
        } else {
            Check.stateCondition(!hasEnabledAutoChunkLoad(),
                    "Tried to set a block to an unloaded chunk with auto chunk load disabled");
            final int chunkX = ChunkUtils.getChunkCoordinate(x);
            final int chunkZ = ChunkUtils.getChunkCoordinate(z);
            loadChunk(chunkX, chunkZ, c -> UNSAFE_setBlock(c, x, y, z, block));
        }
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
    private void UNSAFE_setBlock(@NotNull Chunk chunk, int x, int y, int z, @NotNull Block block) {

        // Cannot place block in a read-only chunk
        if (chunk.isReadOnly()) {
            return;
        }

        synchronized (chunk) {

            // Refresh the last block change time
            this.lastBlockChangeTime = System.currentTimeMillis();

            final BlockPosition blockPosition = new BlockPosition(x, y, z);

            if (isAlreadyChanged(blockPosition, block)) { // do NOT change the block again.
                // Avoids StackOverflowExceptions when onDestroy tries to destroy the block itself
                // This can happen with nether portals which break the entire frame when a portal block is broken
                return;
            }
            setAlreadyChanged(blockPosition, block);

            final BlockHandler previousHandler = chunk.getBlock(blockPosition)
                    .getHandler();

            // Change id based on neighbors
            block = executeBlockPlacementRule(block, blockPosition);

            // Set the block
            chunk.setBlock(x, y, z, block);

            // Refresh neighbors since a new block has been placed
            executeNeighboursBlockPlacementRule(blockPosition);

            // Refresh player chunk block
            sendBlockChange(chunk, blockPosition, block);

            if (previousHandler != null) {
                // Previous destroy
                previousHandler.onDestroy(this, blockPosition);
            }
            final BlockHandler handler = block.getHandler();
            if (handler != null) {
                // New placement
                handler.onPlace(this, blockPosition);
            }
        }
    }

    private void setAlreadyChanged(@NotNull BlockPosition blockPosition, Block block) {
        currentlyChangingBlocks.put(blockPosition, block);
    }

    /**
     * Has this block already changed since last update?
     * Prevents StackOverflow with blocks trying to modify their position in onDestroy or onPlace.
     *
     * @param blockPosition the block position
     * @param block         the block
     * @return true if the block changed since the last update
     */
    private boolean isAlreadyChanged(@NotNull BlockPosition blockPosition, @NotNull Block block) {
        final Block changedBlock = currentlyChangingBlocks.get(blockPosition);
        if (changedBlock == null)
            return false;
        return changedBlock.getId() == block.getId();
    }

    /**
     * Calls the {@link BlockPlacementRule} for the specified block state id.
     *
     * @param block         the block to modify
     * @param blockPosition the block position
     * @return the modified block state id
     */
    private Block executeBlockPlacementRule(Block block, @NotNull BlockPosition blockPosition) {
        final BlockPlacementRule blockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(block);
        if (blockPlacementRule != null) {
            return blockPlacementRule.blockUpdate(this, blockPosition, block);
        }
        return block;
    }

    /**
     * Executed when a block is modified, this is used to modify the states of neighbours blocks.
     * <p>
     * For example, this can be used for redstone wires which need an understanding of its neighborhoods to take the right shape.
     *
     * @param blockPosition the position of the modified block
     */
    private void executeNeighboursBlockPlacementRule(@NotNull BlockPosition blockPosition) {
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

                    final Block neighborBlock = chunk.getBlock(neighborX, neighborY, neighborZ);
                    final BlockPlacementRule neighborBlockPlacementRule = BLOCK_MANAGER.getBlockPlacementRule(neighborBlock);
                    if (neighborBlockPlacementRule != null) {
                        final BlockPosition neighborPosition = new BlockPosition(neighborX, neighborY, neighborZ);
                        final Block newNeighborBlock = neighborBlockPlacementRule.blockUpdate(this,
                                neighborPosition, neighborBlock);
                        if (neighborBlock != newNeighborBlock) {
                            setBlock(neighborPosition, newNeighborBlock);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull BlockPosition blockPosition) {
        player.resetTargetBlock();

        final Chunk chunk = getChunkAt(blockPosition);
        Check.notNull(chunk, "You cannot break blocks in a null chunk!");

        // Cancel if the chunk is read-only
        if (chunk.isReadOnly()) {
            return false;
        }

        // Chunk unloaded, stop here
        if (!ChunkUtils.isLoaded(chunk))
            return false;

        final int x = blockPosition.getX();
        final int y = blockPosition.getY();
        final int z = blockPosition.getZ();

        final Block block = getBlock(x, y, z);

        // The player probably have a wrong version of this chunk section, send it
        if (block.isAir()) {
            chunk.sendChunkSectionUpdate(ChunkUtils.getSectionAt(y), player);
            return false;
        }

        PlayerBlockBreakEvent blockBreakEvent = new PlayerBlockBreakEvent(player, block, Block.AIR, blockPosition);
        player.callEvent(PlayerBlockBreakEvent.class, blockBreakEvent);
        final boolean allowed = !blockBreakEvent.isCancelled();
        if (allowed) {
            // Break or change the broken block based on event result
            final Block resultBlock = blockBreakEvent.getResultBlock();
            setBlock(x, y, z, resultBlock);

            // Send the block break effect packet
            {
                EffectPacket effectPacket = new EffectPacket();
                effectPacket.effectId = 2001; // Block break + block break sound
                effectPacket.position = blockPosition;
                effectPacket.data = resultBlock.getStateId();
                effectPacket.disableRelativeVolume = false;

                PacketUtils.sendGroupedPacket(chunk.getViewers(), effectPacket,
                        (viewer) -> {
                            // Prevent the block breaker to play the particles and sound two times
                            return !viewer.equals(player);
                        });
            }

        }

        return allowed;
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            OptionalCallback.execute(callback, chunk);
        } else {
            // Retrieve chunk from somewhere else (file or create a new one using the ChunkGenerator)
            retrieveChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            // Chunk already loaded
            OptionalCallback.execute(callback, chunk);
        } else {
            if (hasEnabledAutoChunkLoad()) {
                // Load chunk from StorageLocation or with ChunkGenerator
                retrieveChunk(chunkX, chunkZ, callback);
            } else {
                // Chunk not loaded, return null
                OptionalCallback.execute(callback, null);
            }
        }
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {
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
     * Saves the World ({@link #getUniqueId()} {@link #getData()}) and call {@link #saveChunksToStorage(Runnable)}.
     * <p>
     * WARNING: {@link #getData()} needs to be a {@link SerializableData} in order to be saved.
     *
     * @param callback the optional callback once the saving is done
     */
    public void saveWorld(@Nullable Runnable callback) {
        Check.notNull(getStorageLocation(), "You cannot save the World if no StorageLocation has been defined");

        this.storageLocation.set(UUID_KEY, getUniqueId(), UUID.class);
        final Data data = getData();
        if (data != null) {
            // Save the world data
            Check.stateCondition(!(data instanceof SerializableData),
                    "World#getData needs to be a SerializableData in order to be saved");
            this.storageLocation.set(DATA_KEY, (SerializableData) getData(), SerializableData.class);
        }

        saveChunksToStorage(callback);
    }

    /**
     * Saves the World without callback.
     *
     * @see #saveWorld(Runnable)
     */
    public void saveWorld() {
        saveWorld(null);
    }

    @Override
    public void saveChunkToStorage(@NotNull Chunk chunk, Runnable callback) {
        this.chunkLoader.saveChunk(chunk, callback);
    }

    @Override
    public void saveChunksToStorage(@Nullable Runnable callback) {
        Collection<Chunk> chunksCollection = chunks.values();
        this.chunkLoader.saveChunks(chunksCollection, callback);
    }

    @Override
    protected void retrieveChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        final boolean loaded = chunkLoader.loadChunk(this, chunkX, chunkZ, chunk -> {
            cacheChunk(chunk);
            UPDATE_MANAGER.signalChunkLoad(chunk);
            // Execute callback and event in the world thread
            scheduleNextTick(world -> {
                callChunkLoadEvent(chunkX, chunkZ);
                OptionalCallback.execute(callback, chunk);
            });
        });

        if (!loaded) {
            // Not found, create a new chunk
            createChunk(chunkX, chunkZ, callback);
        }
    }

    @Override
    protected void createChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        Biome[] biomes = new Biome[Biome.getBiomeCount(getDimensionType())];
        if (chunkGenerator == null) {
            Arrays.fill(biomes, MinecraftServer.getBiomeManager().getById(0));
        } else {
            chunkGenerator.fillBiomes(biomes, chunkX, chunkZ);
        }

        final Chunk chunk = chunkSupplier.createChunk(this, biomes, chunkX, chunkZ);
        Check.notNull(chunk, "Chunks supplied by a ChunkSupplier cannot be null.");

        cacheChunk(chunk);

        if (chunkGenerator != null && chunk.shouldGenerate()) {
            // Execute the chunk generator to populate the chunk
            final ChunkGenerationBatch chunkBatch = new ChunkGenerationBatch(this, chunk);

            chunkBatch.generate(chunkGenerator, callback);
        } else {
            // No chunk generator, execute the callback with the empty chunk
            OptionalCallback.execute(callback, chunk);
        }

        UPDATE_MANAGER.signalChunkLoad(chunk);
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
    public boolean isInVoid(@NotNull Position position) {
        // TODO: customizable
        return position.getY() < -64;
    }

    /**
     * Changes which type of {@link Chunk} implementation to use once one needs to be loaded.
     * <p>
     * Uses {@link DynamicChunk} by default.
     * <p>
     * WARNING: if you need to save this World's chunks later,
     * the code needs to be predictable for {@link IChunkLoader#loadChunk(World, int, int, ChunkCallback)}
     * to create the correct type of {@link Chunk}. tl;dr: Need chunk save = no random type.
     *
     * @param chunkSupplier the new {@link ChunkSupplier} of this World, chunks need to be non-null
     * @throws NullPointerException if {@code chunkSupplier} is null
     */
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
     * Gets all the {@link SharedWorld} linked to this container.
     *
     * @return an unmodifiable {@link List} containing all the {@link SharedWorld} linked to this container
     */
    public List<SharedWorld> getSharedWorlds() {
        return Collections.unmodifiableList(sharedWorlds);
    }

    /**
     * Gets if this World has {@link SharedWorld} linked to it.
     *
     * @return true if {@link #getSharedWorlds()} is not empty
     */
    public boolean hasSharedWorlds() {
        return !sharedWorlds.isEmpty();
    }

    /**
     * Assigns a {@link SharedWorld} to this container.
     * <p>
     * Only used by {@link WorldManager}, mostly unsafe.
     *
     * @param sharedWorld the shared World to assign to this container
     */
    protected void addSharedWorld(SharedWorld sharedWorld) {
        this.sharedWorlds.add(sharedWorld);
    }

    /**
     * Copies all the chunks of this World and create a new WorldContainer with all of them.
     * <p>
     * Chunks are copied with {@link Chunk#copy(World, int, int)},
     * {@link UUID} is randomized, {@link DimensionType} is passed over and the {@link StorageLocation} is null.
     *
     * @return an {@link WorldContainer} with the exact same chunks as 'this'
     * @see #getSrcWorld() to retrieve the "creation source" of the copied World
     */
    public synchronized WorldContainer copy() {
        WorldContainer copiedWorld = new WorldContainer(UUID.randomUUID(), getDimensionType(), null);
        copiedWorld.srcWorld = this;
        copiedWorld.lastBlockChangeTime = lastBlockChangeTime;

        for (Chunk chunk : chunks.values()) {
            final int chunkX = chunk.getChunkX();
            final int chunkZ = chunk.getChunkZ();

            final Chunk copiedChunk = chunk.copy(copiedWorld, chunkX, chunkZ);

            copiedWorld.cacheChunk(copiedChunk);
            UPDATE_MANAGER.signalChunkLoad(copiedChunk);
        }

        return copiedWorld;
    }

    /**
     * Gets the World from which this one has been copied.
     * <p>
     * Only present if this World has been created with {@link WorldContainer#copy()}.
     *
     * @return the World source, null if not created by a copy
     * @see #copy() to create a copy of this World with 'this' as the source
     */
    @Nullable
    public WorldContainer getSrcWorld() {
        return srcWorld;
    }

    /**
     * Gets the last time at which a block changed.
     *
     * @return the time at which the last block changed in milliseconds, 0 if never
     */
    public long getLastBlockChangeTime() {
        return lastBlockChangeTime;
    }

    /**
     * Signals the World that a block changed.
     * <p>
     * Useful if you change blocks values directly using a {@link Chunk} object.
     */
    public void refreshLastBlockChangeTime() {
        this.lastBlockChangeTime = System.currentTimeMillis();
    }

    /**
     * Adds a {@link Chunk} to the internal World map.
     * <p>
     * WARNING: the chunk will not automatically be sent to players and
     * {@link net.minestom.server.UpdateManager#signalChunkLoad(Chunk)} must be called manually.
     *
     * @param chunk the chunk to cache
     */
    public void cacheChunk(@NotNull Chunk chunk) {
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
     * Gets all the World's chunks.
     *
     * @return the chunks of this World
     */
    @NotNull
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
     * Gets the {@link IChunkLoader} of this World.
     *
     * @return the {@link IChunkLoader} of this World
     */
    public IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    /**
     * Changes the {@link IChunkLoader} of this World (to change how chunks are retrieved when not already loaded).
     *
     * @param chunkLoader the new {@link IChunkLoader}
     */
    public void setChunkLoader(IChunkLoader chunkLoader) {
        this.chunkLoader = chunkLoader;
    }

    /**
     * Sends a {@link BlockChangePacket} at the specified {@link BlockPosition} to set the block as {@code blockStateId}.
     * <p>
     * WARNING: this does not change the internal block data, this is strictly visual for the players.
     *
     * @param chunk         the chunk where the block is
     * @param blockPosition the block position
     * @param block         the new block
     */
    private void sendBlockChange(@NotNull Chunk chunk, @NotNull BlockPosition blockPosition, @NotNull Block block) {
        BlockChangePacket blockChangePacket = new BlockChangePacket();
        blockChangePacket.blockPosition = blockPosition;
        blockChangePacket.blockStateId = block.getStateId();
        chunk.sendPacketToViewers(blockChangePacket);
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
     * Unloads all waiting chunks.
     * <p>
     * Unsafe because it has to be done on the same thread as the world/chunks tick update.
     */
    protected void UNSAFE_unloadChunks() {
        if (scheduledChunksToRemove.isEmpty()) {
            // Fast exit
            return;
        }
        synchronized (scheduledChunksToRemove) {
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

                UPDATE_MANAGER.signalChunkUnload(chunk);
            }
            this.scheduledChunksToRemove.clear();
        }
    }

    private void callChunkLoadEvent(int chunkX, int chunkZ) {
        WorldChunkLoadEvent chunkLoadEvent = new WorldChunkLoadEvent(this, chunkX, chunkZ);
        callEvent(WorldChunkLoadEvent.class, chunkLoadEvent);
    }

    private void callChunkUnloadEvent(int chunkX, int chunkZ) {
        WorldChunkUnloadEvent chunkUnloadEvent = new WorldChunkUnloadEvent(this, chunkX, chunkZ);
        callEvent(WorldChunkUnloadEvent.class, chunkUnloadEvent);
    }
}