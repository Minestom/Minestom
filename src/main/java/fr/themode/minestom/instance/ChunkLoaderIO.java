package fr.themode.minestom.instance;

import fr.themode.minestom.instance.batch.ChunkBatch;
import fr.themode.minestom.io.IOManager;
import fr.themode.minestom.utils.CompressionUtils;
import fr.themode.minestom.utils.SerializerUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

public class ChunkLoaderIO {

    private static File getChunkFile(int chunkX, int chunkZ, File folder) {
        return new File(folder, getChunkFileName(chunkX, chunkZ));
    }

    private static String getChunkFileName(int chunkX, int chunkZ) {
        return "chunk." + chunkX + "." + chunkZ + ".data";
    }

    protected void saveChunk(Chunk chunk, File folder, Runnable callback) {
        IOManager.submit(() -> {
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

            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(CompressionUtils.getDecompressedData(array)));

            ChunkBatch chunkBatch = null;
            try {
                Biome biome = Biome.fromId(stream.readByte());
                Chunk chunk = new Chunk(biome, chunkX, chunkZ);

                chunkBatch = instance.createChunkBatch(chunk);

                while (true) {
                    // TODO block data
                    int index = stream.readInt();
                    boolean isCustomBlock = stream.readBoolean();
                    short blockId = stream.readShort();

                    byte[] chunkPos = SerializerUtils.indexToChunkPosition(index);
                    byte x = chunkPos[0];
                    byte y = chunkPos[1];
                    byte z = chunkPos[2];
                    if (isCustomBlock) {
                        chunkBatch.setCustomBlock(x, y, z, blockId);
                    } else {
                        chunkBatch.setBlock(x, y, z, blockId);
                    }
                }
            } catch (EOFException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }

            chunkBatch.flush(c -> callback.accept(c)); // Success, null if file isn't properly encoded
        });
    }


}
