package net.minestom.scratch.tools;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.world.DimensionType;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public final class ScratchBlockTools {
    /**
     * Utils to manage a world blocks.
     */
    public static final class World {
        private final DimensionType dimensionType;
        private final int minSection;
        private final int maxSection;
        private final int sectionCount;

        public World(DimensionType dimensionType) {
            this.dimensionType = dimensionType;
            this.minSection = dimensionType.getMinY() / Chunk.CHUNK_SECTION_SIZE;
            this.maxSection = (dimensionType.getMinY() + dimensionType.getHeight()) / Chunk.CHUNK_SECTION_SIZE;
            this.sectionCount = maxSection - minSection;
        }

        public ChunkDataPacket generatePacket(int chunkX, int chunkZ) {
            final byte[] data = ObjectPool.PACKET_POOL.use(buffer ->
                    NetworkBuffer.makeArray(networkBuffer -> {
                        for (int i = 0; i < sectionCount; i++) {
                            Palette blockPalette = Palette.blocks();
                            Palette biomePalette = Palette.biomes();
                            if (i < 7) {
                                blockPalette.fill(Block.STONE.stateId());
                            }
                            networkBuffer.write(SHORT, (short) blockPalette.count());
                            networkBuffer.write(blockPalette);
                            networkBuffer.write(biomePalette);
                        }
                    }));
            return new ChunkDataPacket(chunkX, chunkZ,
                    new ChunkData(NBTCompound.EMPTY, data, Map.of()),
                    new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())
            );
        }

        public DimensionType dimensionType() {
            return dimensionType;
        }
    }
}
