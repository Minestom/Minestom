package fr.themode.minestom.instance;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.CompressionUtils;
import fr.themode.minestom.utils.SerializerUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChunkLoaderIO {

    private static final int COMPRESSION_LEVEL = 1;

    private ExecutorService chunkLoaderPool = Executors.newFixedThreadPool(Main.THREAD_COUNT_CHUNK_IO);

    private static File getChunkFile(int chunkX, int chunkZ, File folder) {
        return new File(folder, getChunkFileName(chunkX, chunkZ));
    }

    private static String getChunkFileName(int chunkX, int chunkZ) {
        return "chunk." + chunkX + "." + chunkZ + ".data";
    }

    protected void saveChunk(Chunk chunk, File folder, Runnable callback) {
        chunkLoaderPool.execute(() -> {
            File chunkFile = getChunkFile(chunk.getChunkX(), chunk.getChunkZ(), folder);
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
        chunkLoaderPool.execute(() -> {
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

            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(CompressionUtils.getDecompressedData(array)));

            Chunk chunk = null;
            try {
                Biome biome = Biome.fromId(stream.readByte());
                chunk = new Chunk(biome, chunkX, chunkZ);

                while (true) {
                    // TODO block data
                    int index = stream.readInt();
                    boolean isCustomBlock = stream.readBoolean();
                    short blockId = stream.readShort();

                    byte[] chunkPos = SerializerUtils.indexToChunkPosition(index);
                    if (isCustomBlock) {
                        chunk.setCustomBlock(chunkPos[0], chunkPos[1], chunkPos[2], blockId);
                    } else {
                        chunk.setBlock(chunkPos[0], chunkPos[1], chunkPos[2], blockId);
                    }
                }
            } catch (EOFException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }

            callback.accept(chunk); // Success, null if file isn't properly encoded
        });
    }


}
