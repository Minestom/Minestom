package net.minestom.server.reader;

import io.netty.buffer.Unpooled;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.function.Consumer;

public class ChunkReader {

    public static void readChunk(byte[] b, Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));

        ChunkBatch chunkBatch = null;
        try {
            Biome biome = Biome.fromId(stream.readByte());
            Chunk chunk = new Chunk(biome, chunkX, chunkZ);

            chunkBatch = instance.createChunkBatch(chunk);

            while (true) {
                int x = stream.readInt();
                int y = stream.readInt();
                int z = stream.readInt();

                short blockId = stream.readShort();
                short customBlockId = stream.readShort();

                boolean hasData = stream.readBoolean();
                Data data = null;

                // Data deserializer
                if (hasData) {
                    int dataLength = stream.readInt();
                    byte[] dataArray = stream.readNBytes(dataLength);
                    data = DataReader.readData(Unpooled.wrappedBuffer(dataArray));
                }

                if (customBlockId != 0) {
                    chunkBatch.setSeparateBlocks(x, y, z, blockId, customBlockId, data);
                } else {
                    chunkBatch.setBlock(x, y, z, blockId, data);
                }
            }
        } catch (EOFException e) {
            // Finished reading
        } catch (IOException e) {
            e.printStackTrace();
        }

        chunkBatch.flush(c -> callback.accept(c)); // Success, null if file isn't properly encoded
    }

}
