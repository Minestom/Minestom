package net.minestom.scratch.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

import static net.minestom.server.network.NetworkBuffer.SHORT;

/**
 * World with a fixed size, chunks outside the provided range cannot be loaded.
 */
@SuppressWarnings("unused")
public final class FixedSizeWorld implements Block.Getter, Block.Setter {
    public static final int CHUNK_SECTION_SIZE = 16;

    private final int width, depth;
    private final DimensionType dimensionType;
    private final int minSection;
    private final int maxSection;
    private final int sectionCount;
    private final Chunk[] chunks;

    private final DynamicRegistry<Biome> biomeRegistry;
    private final Generator generator;

    public FixedSizeWorld(int width, int depth, DimensionType dimensionType, DynamicRegistry<Biome> biomeRegistry, Generator generator) {
        this.width = width;
        this.depth = depth;
        this.dimensionType = dimensionType;
        this.minSection = dimensionType.minY() / CHUNK_SECTION_SIZE;
        this.maxSection = (dimensionType.minY() + dimensionType.height()) / CHUNK_SECTION_SIZE;
        this.sectionCount = maxSection - minSection;
        this.chunks = new Chunk[width * depth];

        this.biomeRegistry = biomeRegistry;
        this.generator = generator;
    }

    public void generateAll() {
        Arrays.setAll(chunks, i -> new Chunk());
    }

    public List<ChunkDataPacket> generateAllPackets() {
        List<ChunkDataPacket> packets = new ArrayList<>(width * depth);
        for (int x = -width / 2; x < width / 2; x++) {
            for (int z = -depth / 2; z < depth / 2; z++) {
                packets.add(generatePacket(x, z));
            }
        }
        return packets;
    }

    public ChunkDataPacket generatePacket(int chunkX, int chunkZ) {
        final Chunk chunk = retrieveChunk(chunkX, chunkZ);
        if (chunk == null) return null;
        final byte[] data = NetworkBuffer.makeArray(networkBuffer -> {
            for (Section section : chunk.sections) {
                networkBuffer.write(SHORT, (short) section.blocks.count());
                networkBuffer.write(Palette.BLOCK_SERIALIZER, section.blocks);
                networkBuffer.write(Palette.BIOME_SERIALIZER, section.biomes);
            }
        });
        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(CompoundBinaryTag.empty(), data, Map.of()),
                new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())
        );
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final Chunk chunk = retrieveChunk(x >> 4, z >> 4);
        if (chunk == null) return;
        final Section section = chunk.sections[(y >> 4) - minSection];
        section.blocks.set(x & 0xF, y & 0xF, z & 0xF, block.stateId());
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final Chunk chunk = retrieveChunk(x >> 4, z >> 4);
        if (chunk == null) return Block.AIR;
        final Section section = chunk.sections[(y >> 4) - minSection];
        final int stateId = section.blocks.get(x & 0xF, y & 0xF, z & 0xF);
        return Block.fromStateId((short) stateId);
    }

    private Chunk retrieveChunk(int chunkX, int chunkZ) {
        final int index = fixedIndex(chunkX + width / 2, chunkZ + depth / 2);
        if (index < 0 || index >= chunks.length) return null;
        Chunk chunk = chunks[index];
        if (chunk == null) chunks[index] = chunk = new Chunk();
        return chunk;
    }

    private int fixedIndex(int x, int z) {
        return x + z * width;
    }

    private final class Chunk {
        private final Section[] sections = new Section[sectionCount];

        {
            Arrays.setAll(sections, i -> new Section());
            // Generate blocks
            GeneratorImpl.GenSection[] genSections = new GeneratorImpl.GenSection[sectionCount];
            Arrays.setAll(genSections, i -> {
                Section section = sections[i];
                return new GeneratorImpl.GenSection(section.blocks, section.biomes);
            });
            var unit = GeneratorImpl.chunk(biomeRegistry, genSections, 0, minSection, 0);
            generator.generate(unit);
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
