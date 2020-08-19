package net.minestom.server.reader;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeManager;

import java.util.function.Consumer;

public class ChunkReader {

    private static final BiomeManager BIOME_MANAGER = MinecraftServer.getBiomeManager();

    public static void readChunk(byte[] b, Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) {
        BinaryReader binaryReader = new BinaryReader(b);

        ChunkBatch chunkBatch = null;
        try {

            Biome[] biomes = new Biome[Chunk.BIOME_COUNT];
            for (int i = 0; i < biomes.length; i++) {
                final byte id = binaryReader.readByte();
                biomes[i] = BIOME_MANAGER.getById(id);
            }

            final Chunk chunk = new DynamicChunk(biomes, chunkX, chunkZ);

            chunkBatch = instance.createChunkBatch(chunk);

            while (true) {
                // Position
                final byte x = binaryReader.readByte();
                final short y = binaryReader.readShort();
                final byte z = binaryReader.readByte();

                // Block type
                final short blockStateId = binaryReader.readShort();
                final short customBlockId = binaryReader.readShort();

                // Data
                Data data = null;
                {
                    final boolean hasData = binaryReader.readBoolean();

                    // Data deserializer
                    if (hasData) {
                        data = DataReader.readData(binaryReader);
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

        chunkBatch.flush(c -> callback.accept(c)); // Success, null if file isn't properly encoded
    }

}
