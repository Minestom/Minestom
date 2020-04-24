package net.minestom.server.instance;

import net.minestom.server.io.ChunkReader;
import net.minestom.server.io.IOManager;
import net.minestom.server.utils.CompressionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ChunkLoaderIO {

    private static File getChunkFile(int chunkX, int chunkZ, File folder) {
        return new File(folder, getChunkFileName(chunkX, chunkZ));
    }

    private static String getChunkFileName(int chunkX, int chunkZ) {
        return "chunk_" + chunkX + "." + chunkZ + ".data";
    }

    protected void saveChunk(Chunk chunk, File folder, Runnable callback) {
        IOManager.submit(() -> {
            File chunkFile = getChunkFile(chunk.getChunkX(), chunk.getChunkZ(), folder);
            if (!chunkFile.exists()) {
                try {
                    chunkFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                byte[] data = chunk.getSerializedData();
                fos.write(CompressionUtils.getCompressedData(data));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (callback != null)
                callback.run();
        });
    }

    protected void loadChunk(int chunkX, int chunkZ, Instance instance, Consumer<Chunk> callback) {
        IOManager.submit(() -> {
            File chunkFile = getChunkFile(chunkX, chunkZ, instance.getFolder());
            if (!chunkFile.exists()) {
                instance.createChunk(chunkX, chunkZ, callback); // Chunk file does not exist, create new chunk
                return;
            }

            byte[] array;
            try {
                array = Files.readAllBytes(getChunkFile(chunkX, chunkZ, instance.getFolder()).toPath());
            } catch (IOException e) {
                e.printStackTrace();
                instance.createChunk(chunkX, chunkZ, callback); // Unknown error, create new chunk by default
                return;
            }

            ChunkReader.readChunk(array, instance, chunkX, chunkZ, true, callback);
        });
    }


}
