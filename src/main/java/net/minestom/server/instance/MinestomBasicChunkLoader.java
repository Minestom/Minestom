package net.minestom.server.instance;

import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IChunkLoader} used by {@link InstanceContainer}
 * which is based on the {@link StorageLocation} and {@link ChunkSupplier} associated to it.
 * <p>
 * It simply save chunk serialized data from {@link Chunk#getSerializedData()}
 * and deserialize it later with {@link Chunk#readChunk(BinaryReader, ChunkCallback)}.
 * <p>
 * The key used in the {@link StorageLocation} is defined by {@link #getChunkKey(int, int)} and should NOT be changed.
 */
public class MinestomBasicChunkLoader implements IChunkLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(MinestomBasicChunkLoader.class);
    private final InstanceContainer instanceContainer;

    /**
     * Create an {@link IChunkLoader} which use a {@link StorageLocation}.
     * <p>
     * The {@link ChunkSupplier} is used to customize which type of {@link Chunk} this loader should use for loading.
     * <p>
     * WARNING: {@link Chunk} implementations do not need to have the same serializing format, be careful.
     *
     * @param instanceContainer the {@link InstanceContainer} linked to this loader
     */
    public MinestomBasicChunkLoader(InstanceContainer instanceContainer) {
        this.instanceContainer = instanceContainer;
    }

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
        final StorageLocation storageLocation = instanceContainer.getStorageLocation();
        if (storageLocation == null) {
            callback.run();
            LOGGER.warn("No storage location to save chunk!");
            return;
        }

        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        final String key = getChunkKey(chunkX, chunkZ);

        // Serialize the chunk
        final byte[] data = chunk.getSerializedData();
        if (data == null) {
            // Chunk cannot be serialized (returned null), stop here
            if (callback != null)
                callback.run();
            return;
        }

        // Save the serialized data to the storage location
        storageLocation.set(key, data);

        if (callback != null)
            callback.run();
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, ChunkCallback callback) {
        final StorageLocation storageLocation = instanceContainer.getStorageLocation();
        final byte[] bytes = storageLocation == null ? null : storageLocation.get(getChunkKey(chunkX, chunkZ));

        if (bytes == null) {
            // Chunk is not saved in the storage location
            return false;
        } else {
            // Found, load from result bytes
            BinaryReader reader = new BinaryReader(bytes);
            // Create the chunk object using the instance's ChunkSupplier to support multiple implementations
            Chunk chunk = instanceContainer.getChunkSupplier().getChunk(instance, null, chunkX, chunkZ);
            // Execute the callback once all blocks are placed (allow for multithreaded implementations)
            chunk.readChunk(reader, callback);
            return true;
        }
    }

    @Override
    public boolean supportsParallelSaving() {
        return true;
    }

    @Override
    public boolean supportsParallelLoading() {
        return true;
    }

    /**
     * Get the chunk key used by the {@link StorageLocation}
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the chunk key
     */
    private static String getChunkKey(int chunkX, int chunkZ) {
        return chunkX + "." + chunkZ;
    }


}
