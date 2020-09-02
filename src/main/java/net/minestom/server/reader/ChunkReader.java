package net.minestom.server.reader;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;

import java.util.function.Consumer;

public class ChunkReader {

    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    /**
     * Read a chunk from a byte array, the array should contain the whole chunk and only it
     *
     * @param b        the byte array containing the chunk
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback the consumer called once the chunk has been read
     */
    public static void readChunk(byte[] b, Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) {
        BinaryReader binaryReader = new BinaryReader(b);

        // Used for blocks data
        Object2ShortMap<String> typeToIndexMap = null;

        ChunkBatch chunkBatch = null;
        try {

            // Get if the chunk has data indexes (used for blocks data)
            final boolean hasIndex = binaryReader.readBoolean();
            if (hasIndex) {
                // Get the data indexes which will be used to read all the individual data
                typeToIndexMap = DataReader.readDataIndexes(binaryReader);
            }

            Biome[] biomes = new Biome[Chunk.BIOME_COUNT];
            for (int i = 0; i < biomes.length; i++) {
                final byte id = binaryReader.readByte();
                biomes[i] = BIOME_MANAGER.getById(id);
            }

            final Chunk chunk = new DynamicChunk(biomes, chunkX, chunkZ);

            chunkBatch = instance.createChunkBatch(chunk);

            while (true) {
                // Position
                final short index = binaryReader.readShort();
                final byte x = ChunkUtils.blockIndexToChunkPositionX(index);
                final short y = ChunkUtils.blockIndexToChunkPositionY(index);
                final byte z = ChunkUtils.blockIndexToChunkPositionZ(index);

                // Block type
                final short blockStateId = binaryReader.readShort();
                final short customBlockId = binaryReader.readShort();

                // Data
                Data data = null;
                {
                    final boolean hasData = binaryReader.readBoolean();
                    // Data deserializer
                    if (hasData) {
                        // Read the data with the deserialized index map
                        data = DataReader.readData(typeToIndexMap, binaryReader);
                    }
                }

                if (customBlockId != 0) {
                    chunkBatch.setSeparateBlocks(x, y, z, blockStateId, customBlockId, data);
                } else {
                    chunkBatch.setBlockStateId(x, y, z, blockStateId, data);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Finished reading
        }

        // Place all the blocks from the batch
        chunkBatch.flush(c -> callback.accept(c)); // Success, null if file isn't properly encoded
    }

}
