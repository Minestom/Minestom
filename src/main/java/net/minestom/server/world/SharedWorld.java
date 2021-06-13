package net.minestom.server.world;

import net.minestom.server.entity.Player;
import net.minestom.server.block.Block;
import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * The {@link SharedWorld} is a World that shares the same chunks as its linked {@link WorldContainer},
 * entities are separated.
 */
public class SharedWorld extends World {

    private final WorldContainer worldContainer;

    public SharedWorld(@NotNull UUID uniqueId, @NotNull WorldContainer worldContainer) {
        super(uniqueId, worldContainer.getDimensionType());
        this.worldContainer = worldContainer;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        this.worldContainer.setBlock(x, y, z, block);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull BlockPosition blockPosition) {
        return worldContainer.breakBlock(player, blockPosition);
    }

    @Override
    public void loadChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.worldContainer.loadChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void loadOptionalChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.worldContainer.loadOptionalChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {
        worldContainer.unloadChunk(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return worldContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public void saveChunkToStorage(@NotNull Chunk chunk, @Nullable Runnable callback) {
        this.worldContainer.saveChunkToStorage(chunk, callback);
    }

    @Override
    public void saveChunksToStorage(@Nullable Runnable callback) {
        worldContainer.saveChunksToStorage(callback);
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.worldContainer.setChunkGenerator(chunkGenerator);
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return worldContainer.getChunkGenerator();
    }

    @NotNull
    @Override
    public Collection<Chunk> getChunks() {
        return worldContainer.getChunks();
    }

    @Override
    public StorageLocation getStorageLocation() {
        return worldContainer.getStorageLocation();
    }

    @Override
    public void setStorageLocation(StorageLocation storageLocation) {
        this.worldContainer.setStorageLocation(storageLocation);
    }

    @Override
    public void retrieveChunk(int chunkX, int chunkZ, @Nullable ChunkCallback callback) {
        this.worldContainer.retrieveChunk(chunkX, chunkZ, callback);
    }

    @Override
    protected void createChunk(int chunkX, int chunkZ, ChunkCallback callback) {
        this.worldContainer.createChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        worldContainer.enableAutoChunkLoad(enable);
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return worldContainer.hasEnabledAutoChunkLoad();
    }

    @Override
    public boolean isInVoid(@NotNull Position position) {
        return worldContainer.isInVoid(position);
    }

    /**
     * Gets the {@link WorldContainer} from where this World takes its chunks from.
     *
     * @return the associated {@link WorldContainer}
     */
    @NotNull
    public WorldContainer getWorldContainer() {
        return worldContainer;
    }
}
