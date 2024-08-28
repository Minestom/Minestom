package net.minestom.scratch.world;

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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static net.minestom.server.coordinate.CoordConversionUtils.chunkIndex;
import static net.minestom.server.network.NetworkBuffer.SHORT;

/**
 * Basic structure to hold block state ids and create chunk data packets.
 * <p>
 * Block entities aren't supported.
 */
public final class PaletteWorld implements Block.Getter, Block.Setter {
    public static final int CHUNK_SECTION_SIZE = 16;

    private final DimensionType dimensionType;
    private final int minSection;
    private final int maxSection;
    private final int sectionCount;
    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();

    private final DynamicRegistry<Biome> biomeRegistry;
    private final Generator generator;

    public PaletteWorld(DimensionType dimensionType, DynamicRegistry<Biome> biomeRegistry, Generator generator) {
        this.dimensionType = dimensionType;
        this.minSection = dimensionType.minY() / CHUNK_SECTION_SIZE;
        this.maxSection = (dimensionType.minY() + dimensionType.height()) / CHUNK_SECTION_SIZE;
        this.sectionCount = maxSection - minSection;

        this.biomeRegistry = biomeRegistry;
        this.generator = generator;
    }

    public ChunkDataPacket generatePacket(int chunkX, int chunkZ) {
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(chunkX, chunkZ), i -> new Chunk());
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
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(x >> 4, z >> 4), i -> new Chunk());
        final Section section = chunk.sections[(y >> 4) - minSection];
        section.blocks.set(x & 0xF, y & 0xF, z & 0xF, block.stateId());
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        final Chunk chunk = chunks.computeIfAbsent(chunkIndex(x >> 4, z >> 4), i -> new Chunk());
        final Section section = chunk.sections[(y >> 4) - minSection];
        final int stateId = section.blocks.get(x & 0xF, y & 0xF, z & 0xF);
        return Block.fromStateId((short) stateId);
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
