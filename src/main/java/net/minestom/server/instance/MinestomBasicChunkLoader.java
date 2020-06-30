package net.minestom.server.instance;

import net.minestom.server.reader.ChunkReader;
import net.minestom.server.storage.StorageFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class MinestomBasicChunkLoader implements IChunkLoader {

    private final static Logger LOGGER = LoggerFactory.getLogger(MinestomBasicChunkLoader.class);
    private StorageFolder storageFolder;

    public MinestomBasicChunkLoader(StorageFolder storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
        if(storageFolder == null) {
            callback.run();
            LOGGER.warn("No folder to save chunk!");
            return;
        }
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        try {
            byte[] data = chunk.getSerializedData();
            storageFolder.set(getChunkKey(chunkX, chunkZ), data);

            if (callback != null)
                callback.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) {
        byte[] bytes = storageFolder == null ? null : storageFolder.get(getChunkKey(chunkX, chunkZ));

        if (bytes == null) {
            return false;
        } else {
            // Found, load from result bytes
            ChunkReader.readChunk(bytes, instance, chunkX, chunkZ, callback);
            return true;
        }
    }

    private static String getChunkKey(int chunkX, int chunkZ) {
        return chunkX + "." + chunkZ;
    }


}
