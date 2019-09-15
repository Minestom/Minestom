package fr.themode.minestom.io;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.instance.Biome;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.batch.ChunkBatch;
import fr.themode.minestom.utils.CompressionUtils;
import fr.themode.minestom.utils.SerializerUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

public class ChunkReader {

    public static void readChunk(byte[] b, Instance instance, int chunkX, int chunkZ, boolean shouldDecompress, Consumer<Chunk> callback) {

        b = shouldDecompress ? CompressionUtils.getDecompressedData(b) : b;

        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));

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
                boolean hasData = stream.readBoolean();
                Data data = null;

                // Data deserializer
                if (hasData) {
                    int dataLength = stream.readInt();
                    byte[] dataArray = stream.readNBytes(dataLength);
                    data = DataReader.readData(dataArray, false);
                }

                byte[] chunkPos = SerializerUtils.indexToChunkPosition(index);
                byte x = chunkPos[0];
                byte y = chunkPos[1];
                byte z = chunkPos[2];
                if (isCustomBlock) {
                    chunkBatch.setCustomBlock(x, y, z, blockId, data);
                } else {
                    chunkBatch.setBlock(x, y, z, blockId, data);
                }
            }
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        chunkBatch.flush(c -> callback.accept(c)); // Success, null if file isn't properly encoded
    }

}
