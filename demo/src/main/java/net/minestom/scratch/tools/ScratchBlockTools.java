package net.minestom.scratch.tools;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public final class ScratchBlockTools {
    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Z = 16;
    public static final int CHUNK_SECTION_SIZE = 16;

    /**
     * Utils to manage a world blocks.
     */
    public static final class World implements Block.Getter, Block.Setter {
        private final DimensionType dimensionType;
        private final int minSection;
        private final int maxSection;
        private final int sectionCount;
        private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

        public World(DimensionType dimensionType) {
            this.dimensionType = dimensionType;
            this.minSection = dimensionType.getMinY() / CHUNK_SECTION_SIZE;
            this.maxSection = (dimensionType.getMinY() + dimensionType.getHeight()) / CHUNK_SECTION_SIZE;
            this.sectionCount = maxSection - minSection;
        }

        public ChunkDataPacket generatePacket(int chunkX, int chunkZ) {
            final Chunk chunk = chunks.computeIfAbsent(ChunkUtils.getChunkIndex(chunkX, chunkZ), i -> new Chunk());
            final byte[] data = NetworkBuffer.makeArray(networkBuffer -> {
                for (int i = 0; i < sectionCount; i++) {
                    final Section section = chunk.sections[i];
                    final Palette blockPalette = section.blocks;
                    networkBuffer.write(SHORT, (short) blockPalette.count());
                    networkBuffer.write(blockPalette);
                    networkBuffer.write(section.biomes);
                }
            });
            return new ChunkDataPacket(chunkX, chunkZ,
                    new ChunkData(NBTCompound.EMPTY, data, Map.of()),
                    new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())
            );
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            final int chunkX = ChunkUtils.getChunkCoordinate(x);
            final int chunkY = ChunkUtils.getChunkCoordinate(y) - minSection;
            final int chunkZ = ChunkUtils.getChunkCoordinate(z);
            final Chunk chunk = chunks.computeIfAbsent(ChunkUtils.getChunkIndex(chunkX, chunkZ), i -> new Chunk());
            final Section section = chunk.sections[chunkY];
            final int blockX = ChunkUtils.toSectionRelativeCoordinate(x);
            final int blockY = ChunkUtils.toSectionRelativeCoordinate(y);
            final int blockZ = ChunkUtils.toSectionRelativeCoordinate(z);
            section.blocks.set(blockX, blockY, blockZ, block.stateId());
        }

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            final int chunkX = ChunkUtils.getChunkCoordinate(x);
            final int chunkY = ChunkUtils.getChunkCoordinate(y) - minSection;
            final int chunkZ = ChunkUtils.getChunkCoordinate(z);
            final Chunk chunk = chunks.computeIfAbsent(ChunkUtils.getChunkIndex(chunkX, chunkZ), i -> new Chunk());
            final Section section = chunk.sections[chunkY];
            final int blockX = ChunkUtils.toSectionRelativeCoordinate(x);
            final int blockY = ChunkUtils.toSectionRelativeCoordinate(y);
            final int blockZ = ChunkUtils.toSectionRelativeCoordinate(z);
            final int stateId = section.blocks.get(blockX, blockY, blockZ);
            return Block.fromStateId((short) stateId);
        }

        private final class Chunk {
            private final Section[] sections = new Section[sectionCount];

            {
                Arrays.setAll(sections, i -> new Section());
                // Generate blocks
                for (int i = 0; i < sectionCount; i++) {
                    final Section section = sections[i];
                    final Palette blockPalette = section.blocks;
                    if (i < 7) {
                        blockPalette.fill(Block.STONE.stateId());
                    }
                }
            }
        }

        private static final class Section {
            private final Palette blocks = Palette.blocks();
            private final Palette biomes = Palette.biomes();
        }

        public DimensionType dimensionType() {
            return dimensionType;
        }
    }
}
