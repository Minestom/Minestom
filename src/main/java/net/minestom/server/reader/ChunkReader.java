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

            Biome[] biomes = new Biome[Chunk.BIOME_COUNT];
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = Biome.fromId(stream.readByte());
            }

            final Chunk chunk = new Chunk(biomes, chunkX, chunkZ);

            chunkBatch = instance.createChunkBatch(chunk);

            while (true) {
                final int x = stream.readInt();
                final int y = stream.readInt();
                final int z = stream.readInt();

                final short blockId = stream.readShort();
                final short customBlockId = stream.readShort();

                final boolean hasData = stream.readBoolean();
                Data data = null;

                // Data deserializer
                if (hasData) {
                    final int dataLength = stream.readInt();
                    final byte[] dataArray = stream.readNBytes(dataLength);
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
