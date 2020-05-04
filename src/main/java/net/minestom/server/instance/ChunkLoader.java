package net.minestom.server.instance;

import net.minestom.server.reader.ChunkReader;
import net.minestom.server.storage.StorageFolder;

import java.io.IOException;
import java.util.function.Consumer;

public class ChunkLoader {

    private static String getChunkKey(int chunkX, int chunkZ) {
        return "chunk_" + chunkX + "." + chunkZ;
    }

    protected void saveChunk(Chunk chunk, StorageFolder storageFolder, Runnable callback) {
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

    protected void loadChunk(Instance instance, int chunkX, int chunkZ, StorageFolder storageFolder, Consumer<Chunk> callback) {
        storageFolder.get(getChunkKey(chunkX, chunkZ), bytes -> {

            if (bytes == null) {
                // Not found, create a new chunk
                instance.createChunk(chunkX, chunkZ, callback);
            } else {
                // Found, load from result bytes
                ChunkReader.readChunk(bytes, instance, chunkX, chunkZ, callback);
            }

        });
    }


}
