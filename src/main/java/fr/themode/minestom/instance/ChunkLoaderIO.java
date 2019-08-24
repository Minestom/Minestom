package fr.themode.minestom.instance;

import com.github.luben.zstd.Zstd;
import fr.themode.minestom.Main;
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
                byte[] decompressedLength = SerializerUtils.intToBytes(data.length);
                byte[] compressed = Zstd.compress(data, COMPRESSION_LEVEL);

                byte[] result = new byte[decompressedLength.length + compressed.length];
                System.arraycopy(decompressedLength, 0, result, 0, decompressedLength.length);
                System.arraycopy(compressed, 0, result, decompressedLength.length, compressed.length);

                fos.write(result);
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

            int decompressedLength = SerializerUtils.bytesToInt(array);

            byte[] compressedChunkData = new byte[array.length - Integer.BYTES];
            System.arraycopy(array, Integer.BYTES, compressedChunkData, 0, compressedChunkData.length); // Remove the decompressed length from the array

            byte[] decompressed = new byte[decompressedLength];
            long result = Zstd.decompress(decompressed, compressedChunkData); // Decompressed in an array with the max size

            array = new byte[(int) result];
            System.arraycopy(decompressed, 0, array, 0, (int) result); // Resize the data array properly


            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(array));

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
