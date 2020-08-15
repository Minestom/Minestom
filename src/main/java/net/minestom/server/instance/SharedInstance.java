package net.minestom.server.instance;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Shared instance is an instance that share the same chunks as instanceContainer,
 * entities are separated.
 */
public class SharedInstance extends Instance {

    private InstanceContainer instanceContainer;

    public SharedInstance(UUID uniqueId, InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer.getDimensionType());
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void refreshBlockStateId(BlockPosition blockPosition, short blockStateId) {
        instanceContainer.refreshBlockStateId(blockPosition, blockStateId);
    }

    @Override
    public boolean breakBlock(Player player, BlockPosition blockPosition) {
        return instanceContainer.breakBlock(player, blockPosition);
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.loadChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.loadOptionalChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        instanceContainer.unloadChunk(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return instanceContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public void saveChunkToStorageFolder(Chunk chunk, Runnable callback) {
        instanceContainer.saveChunkToStorageFolder(chunk, callback);
    }

    @Override
    public void saveChunksToStorageFolder(Runnable callback) {
        instanceContainer.saveChunksToStorageFolder(callback);
    }

    @Override
    public BlockBatch createBlockBatch() {
        return instanceContainer.createBlockBatch();
    }

    @Override
    public ChunkBatch createChunkBatch(Chunk chunk) {
        return instanceContainer.createChunkBatch(chunk);
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        instanceContainer.setChunkGenerator(chunkGenerator);
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return instanceContainer.getChunkGenerator();
    }

    @Override
    public Collection<Chunk> getChunks() {
        return instanceContainer.getChunks();
    }

    @Override
    public StorageFolder getStorageFolder() {
        return instanceContainer.getStorageFolder();
    }

    @Override
    public void setStorageFolder(StorageFolder storageFolder) {
        instanceContainer.setStorageFolder(storageFolder);
    }

    @Override
    public void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.retrieveChunk(chunkX, chunkZ, callback);
    }

    @Override
    protected void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.createChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        instanceContainer.enableAutoChunkLoad(enable);
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return instanceContainer.hasEnabledAutoChunkLoad();
    }

    @Override
    public boolean isInVoid(Position position) {
        return instanceContainer.isInVoid(position);
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, Data data) {
        instanceContainer.setBlockStateId(x, y, z, blockStateId, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        instanceContainer.setCustomBlock(x, y, z, customBlockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        instanceContainer.setSeparateBlocks(x, y, z, blockStateId, customBlockId, data);
    }

    @Override
    public void scheduleUpdate(int time, TimeUnit unit, BlockPosition position) {
        instanceContainer.scheduleUpdate(time, unit, position);
    }

    public InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }
}
