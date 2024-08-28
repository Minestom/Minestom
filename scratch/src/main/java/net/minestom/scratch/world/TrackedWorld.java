package net.minestom.scratch.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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

import static net.minestom.server.coordinate.CoordConversionUtils.blockIndex;
import static net.minestom.server.coordinate.CoordConversionUtils.chunkIndex;
import static net.minestom.server.network.NetworkBuffer.SHORT;

/**
 * World storing state ids and block entities.
 * <p>
 * Blocks are additionally
 */
public final class TrackedWorld implements Block.Getter, Block.Setter {
    public static final int CHUNK_SECTION_SIZE = 16;

    private final DimensionType dimensionType;
    private final int minSection;
    private final int maxSection;
    private final int sectionCount;
    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final DynamicRegistry<Biome> biomeRegistry;
    private final Generator generator;

    public TrackedWorld(DimensionType dimensionType, DynamicRegistry<Biome> biomeRegistry, Generator generator) {
        this.dimensionType = dimensionType;
        this.minSection = dimensionType.minY() / CHUNK_SECTION_SIZE;
        this.maxSection = (dimensionType.minY() + dimensionType.height()) / CHUNK_SECTION_SIZE;
        this.sectionCount = maxSection - minSection;

        this.biomeRegistry = biomeRegistry;
        this.generator = generator;
    }

    public ChunkDataPacket generatePacket(int chunkX, int chunkZ) {
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(chunkX, chunkZ), i -> new Chunk(chunkX, chunkZ));
        final byte[] data = NetworkBuffer.makeArray(networkBuffer -> {
            for (Section section : chunk.sections) {
                networkBuffer.write(SHORT, (short) section.blocks.count());
                networkBuffer.write(Palette.BLOCK_SERIALIZER, section.blocks);
                networkBuffer.write(Palette.BIOME_SERIALIZER, section.biomes);
            }
        });

        Map<Integer, Block> blockEntities = new HashMap<>();
        for (Int2ObjectMap.Entry<Block> entry : chunk.blockEntities.int2ObjectEntrySet()) {
            final int blockIndex = entry.getIntKey();
            final Block block = entry.getValue();
            blockEntities.put(blockIndex, block);
        }

        return new ChunkDataPacket(chunkX, chunkZ,
                new ChunkData(CompoundBinaryTag.empty(), data, blockEntities),
                new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of())
        );
    }

    public List<Section> loadedSections() {
        List<Section> sections = new ArrayList<>();
        for (Chunk chunk : chunks.values()) {
            sections.addAll(Arrays.asList(chunk.sections));
        }
        return sections;
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(chunkX, chunkZ), i -> new Chunk(chunkX, chunkZ));
        final int blockIndex = blockIndex(x, y, z);
        if (block.registry().isBlockEntity() || block.nbt() != null) chunk.blockEntities.put(blockIndex, block);
        else chunk.blockEntities.remove(blockIndex);
        final Section section = chunk.sections[(y >> 4) - minSection];
        section.blocks.set(x & 0xF, y & 0xF, z & 0xF, block.stateId());
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Block.Getter.Condition condition) {
        final int chunkX = x >> 4;
        final int chunkZ = z >> 4;
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(chunkX, chunkZ), i -> new Chunk(chunkX, chunkZ));
        final Section section = chunk.sections[(y >> 4) - minSection];
        final int stateId = section.blocks.get(x & 0xF, y & 0xF, z & 0xF);
        return Block.fromStateId((short) stateId);
    }

    public final class Chunk {
        private final Section[] sections = new Section[sectionCount];
        private final Int2ObjectMap<Block> blockEntities = new Int2ObjectOpenHashMap<>();

        public Chunk(int chunkX, int chunkZ) {
            Arrays.setAll(sections, i -> new Section(chunkX, minSection + i, chunkZ));
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

    public record Section(int sectionX, int sectionY, int sectionZ,
                          Palette blocks, Palette biomes) {
        public Section(int sectionX, int sectionY, int sectionZ) {
            this(sectionX, sectionY, sectionZ, Palette.blocks(), Palette.biomes());
        }
    }

    public DimensionType dimensionType() {
        return dimensionType;
    }
}
