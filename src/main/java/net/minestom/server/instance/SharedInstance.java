package net.minestom.server.instance;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.UUID;

/**
 * The {@link SharedInstance} is an instance that shares the same chunks as its linked {@link InstanceContainer},
 * entities are separated.
 */
public class SharedInstance extends Instance {

    private final InstanceContainer instanceContainer;

    public SharedInstance(@NotNull UUID uniqueId, @NotNull InstanceContainer instanceContainer) {
        super(uniqueId, instanceContainer.getDimensionType());
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void refreshBlockStateId(@NotNull BlockPosition blockPosition, short blockStateId) {
        this.instanceContainer.refreshBlockStateId(blockPosition, blockStateId);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull BlockPosition blockPosition) {
        return instanceContainer.breakBlock(player, blockPosition);
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.instanceContainer.loadChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.instanceContainer.loadOptionalChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {
        instanceContainer.unloadChunk(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return instanceContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public void saveChunkToStorage(@NotNull Chunk chunk, @Nullable Runnable callback) {
        this.instanceContainer.saveChunkToStorage(chunk, callback);
    }

    @Override
    public void saveChunksToStorage(@Nullable Runnable callback) {
        instanceContainer.saveChunksToStorage(callback);
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.instanceContainer.setChunkGenerator(chunkGenerator);
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return instanceContainer.getChunkGenerator();
    }

    @NotNull
    @Override
    public Collection<Chunk> getChunks() {
        return instanceContainer.getChunks();
    }

    @Override
    public StorageLocation getStorageLocation() {
        return instanceContainer.getStorageLocation();
    }

    @Override
    public void setStorageLocation(StorageLocation storageLocation) {
        this.instanceContainer.setStorageLocation(storageLocation);
    }

    @Override
    public void retrieveChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.instanceContainer.retrieveChunk(chunkX, chunkZ, callback);
    }

    @Override
    protected void createChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        this.instanceContainer.createChunk(chunkX, chunkZ, callback);
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
    public boolean isInVoid(@NotNull Position position) {
        return instanceContainer.isInVoid(position);
    }

    @Override
    public void setBlockStateId(int x, int y, int z, short blockStateId, Data data) {
        this.instanceContainer.setBlockStateId(x, y, z, blockStateId, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short customBlockId, Data data) {
        this.instanceContainer.setCustomBlock(x, y, z, customBlockId, data);
    }

    @Override
    public void setSeparateBlocks(int x, int y, int z, short blockStateId, short customBlockId, Data data) {
        this.instanceContainer.setSeparateBlocks(x, y, z, blockStateId, customBlockId, data);
    }

    @Override
    public void scheduleUpdate(int time, @NotNull TemporalUnit unit, @NotNull BlockPosition position) {
        this.instanceContainer.scheduleUpdate(time, unit, position);
    }

    /**
     * Gets the {@link InstanceContainer} from where this instance takes its chunks from.
     *
     * @return the associated {@link InstanceContainer}
     */
    @NotNull
    public InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }
}
