package net.minestom.server.instance;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        return instanceContainer.loadChunk(chunkX, chunkZ);
    }

    @Override
    public CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        return instanceContainer.loadOptionalChunk(chunkX, chunkZ);
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
    public CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        return instanceContainer.saveChunkToStorage(chunk);
    }

    @Override
    public CompletableFuture<Void> saveChunksToStorage() {
        return instanceContainer.saveChunksToStorage();
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
    public CompletableFuture<Chunk> retrieveChunk(int chunkX, int chunkZ) {
        return instanceContainer.retrieveChunk(chunkX, chunkZ);
    }

    @NotNull
    @Override
    protected CompletableFuture<Chunk> createChunk(int chunkX, int chunkZ) {
        return instanceContainer.createChunk(chunkX, chunkZ);
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
    public void scheduleUpdate(int time, @NotNull TimeUnit unit, @NotNull BlockPosition position) {
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
