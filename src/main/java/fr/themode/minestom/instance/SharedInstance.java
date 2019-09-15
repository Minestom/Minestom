package fr.themode.minestom.instance;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.batch.BlockBatch;
import fr.themode.minestom.instance.batch.ChunkBatch;
import fr.themode.minestom.utils.BlockPosition;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Shared instance is an instance that share the same chunks as instanceContainer, entities are separated.
 */
public class SharedInstance extends Instance {

    private InstanceContainer instanceContainer;

    protected SharedInstance(UUID uniqueId, InstanceContainer instanceContainer) {
        super(uniqueId);
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void breakBlock(Player player, BlockPosition blockPosition) {
        instanceContainer.breakBlock(player, blockPosition);
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
    public Chunk getChunk(int chunkX, int chunkZ) {
        return instanceContainer.getChunk(chunkX, chunkZ);
    }

    @Override
    public void saveToFolder(Runnable callback) {
        instanceContainer.saveToFolder(callback);
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
    public Collection<Chunk> getChunks() {
        return instanceContainer.getChunks();
    }

    @Override
    public File getFolder() {
        return instanceContainer.getFolder();
    }

    @Override
    public void setFolder(File folder) {
        instanceContainer.setFolder(folder);
    }

    @Override
    public void sendChunkUpdate(Player player, Chunk chunk) {
        instanceContainer.sendChunkUpdate(player, chunk);
    }

    @Override
    public void sendChunkSectionUpdate(Chunk chunk, int section, Player player) {
        instanceContainer.sendChunkSectionUpdate(chunk, section, player);
    }

    @Override
    public void retrieveChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.retrieveChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void createChunk(int chunkX, int chunkZ, Consumer<Chunk> callback) {
        instanceContainer.createChunk(chunkX, chunkZ, callback);
    }

    @Override
    public void sendChunks(Player player) {
        instanceContainer.sendChunks(player);
    }

    @Override
    public void sendChunk(Player player, Chunk chunk) {
        instanceContainer.sendChunk(player, chunk);
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
    public void setBlock(int x, int y, int z, short blockId, Data data) {
        instanceContainer.setBlock(x, y, z, blockId, data);
    }

    @Override
    public void setCustomBlock(int x, int y, int z, short blockId, Data data) {
        instanceContainer.setBlock(x, y, z, blockId, data);
    }

    public InstanceContainer getContainer() {
        return instanceContainer;
    }
}
