package net.minestom.server.instance;

import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IChunkLoader} used by default by {@link InstanceContainer}
 * which is based on the {@link StorageLocation} associated to it
 * <p>
 * It simply save chunk serialized data from {@link Chunk#getSerializedData()}
 * and deserialize it later with {@link Chunk#readChunk(BinaryReader, ChunkCallback)}
 * <p>
 * The key used in the {@link StorageLocation} is defined by {@link #getChunkKey(int, int)} and should NOT be changed
 */
public class MinestomBasicChunkLoader implements IChunkLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(MinestomBasicChunkLoader.class);
    private final StorageLocation storageLocation;

    public MinestomBasicChunkLoader(StorageLocation storageLocation) {
        this.storageLocation = storageLocation;
    }

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
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
        final byte[] bytes = storageLocation == null ? null : storageLocation.get(getChunkKey(chunkX, chunkZ));

        if (bytes == null) {
            // Chunk is not saved in the storage location
            return false;
        } else {
            // Found, load from result bytes
            BinaryReader reader = new BinaryReader(bytes);
            Chunk chunk = new DynamicChunk(instance, null, chunkX, chunkZ);
            chunk.readChunk(reader, callback);
            return true;
        }
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
