package net.minestom.server.instance;

import net.minestom.server.storage.StorageLocation;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final byte[] data = chunk.getSerializedData();
        if (data == null) {
            if (callback != null)
                callback.run();
            return;
        }

        storageLocation.set(key, data);

        if (callback != null)
            callback.run();
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, ChunkCallback callback) {
        final byte[] bytes = storageLocation == null ? null : storageLocation.get(getChunkKey(chunkX, chunkZ));

        if (bytes == null) {
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
