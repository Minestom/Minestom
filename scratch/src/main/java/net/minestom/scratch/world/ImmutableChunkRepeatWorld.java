package net.minestom.scratch.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public final class ImmutableChunkRepeatWorld {
    public static final int CHUNK_SECTION_SIZE = 16;

    private final DimensionType dimensionType;
    private final ChunkData chunkData;
    private final LightData lightData;

    public ImmutableChunkRepeatWorld(DimensionType dimensionType, DynamicRegistry<Biome> biomeRegistry, Generator generator) {
        this.dimensionType = dimensionType;
        final int minSection = dimensionType.minY() / CHUNK_SECTION_SIZE;
        final int maxSection = (dimensionType.minY() + dimensionType.height()) / CHUNK_SECTION_SIZE;
        final int sectionCount = maxSection - minSection;

        GeneratorImpl.GenSection[] sections = new GeneratorImpl.GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GeneratorImpl.GenSection());
        var unit = GeneratorImpl.chunk(biomeRegistry, sections, 0, minSection, 0);
        generator.generate(unit);

        final byte[] data = NetworkBuffer.makeArray(networkBuffer -> {
            for (GeneratorImpl.GenSection section : sections) {
                networkBuffer.write(SHORT, (short) section.blocks().count());
                networkBuffer.write(Palette.BLOCK_SERIALIZER, section.blocks());
                networkBuffer.write(Palette.BIOME_SERIALIZER, section.biomes());
            }
        });

        this.chunkData = new ChunkData(CompoundBinaryTag.empty(), data, Map.of());
        this.lightData = new LightData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of());
    }

    public ChunkDataPacket chunkPacket(int chunkX, int chunkZ) {
        return new ChunkDataPacket(chunkX, chunkZ, chunkData, lightData);
    }

    public DimensionType dimensionType() {
        return dimensionType;
    }
}
