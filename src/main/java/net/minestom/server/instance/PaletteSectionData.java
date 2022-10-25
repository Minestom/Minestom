package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

public final class PaletteSectionData implements Writeable {
    private Palette blockPalette;
    private Palette biomePalette;
    private byte[] skyLight;
    private byte[] blockLight;

    private PaletteSectionData(Palette blockPalette, Palette biomePalette,
                               byte[] skyLight, byte[] blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    public PaletteSectionData() {
        this(Palette.blocks(), Palette.biomes(),
                new byte[0], new byte[0]);
    }

    public static PaletteSectionData copyFromGetter(int chunkX, int sectionY, int chunkZ, Block.Getter getter,
                                                    Function<Block, Integer> idRetriever) {
        final PaletteSectionData section = new PaletteSectionData();
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    final int blockX = chunkX * Chunk.CHUNK_SIZE_X + x;
                    final int blockY = sectionY * Chunk.CHUNK_SECTION_SIZE + y;
                    final int blockZ = chunkZ * Chunk.CHUNK_SIZE_Z + z;
                    final Block block = getter.getBlock(blockX, blockY, blockZ);
                    section.blockPalette.set(x, y, z, idRetriever.apply(block));
                }
            }
        }
        return section;
    }

    public Palette blockPalette() {
        return blockPalette;
    }

    public Palette biomePalette() {
        return biomePalette;
    }

    public ByteList getSkyLight() {
        return ByteList.of(skyLight);
    }

    public void setSkyLight(ByteList skyLight) {
        this.skyLight = skyLight.toByteArray();
    }

    public ByteList getBlockLight() {
        return ByteList.of(blockLight);
    }

    public void setBlockLight(ByteList blockLight) {
        this.blockLight = blockLight.toByteArray();
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.skyLight = new byte[0];
        this.blockLight = new byte[0];
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull PaletteSectionData clone() {
        return new PaletteSectionData(blockPalette.clone(), biomePalette.clone(),
                skyLight.clone(), blockLight.clone());
    }

    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort((short) blockPalette.count());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }

    public @NotNull Section sectionView() {
        return new SectionView();
    }

    private class SectionView implements Section {
        @Override
        public ByteList getSkyLight() {
            return PaletteSectionData.this.getSkyLight();
        }

        @Override
        public void setSkyLight(ByteList skyLight) {
            PaletteSectionData.this.setSkyLight(skyLight);
        }

        @Override
        public ByteList getBlockLight() {
            return PaletteSectionData.this.getBlockLight();
        }

        @Override
        public void setBlockLight(ByteList blockLight) {
            PaletteSectionData.this.setBlockLight(blockLight);
        }

        @Override
        public void clear() {
            PaletteSectionData.this.clear();
        }

        @Override
        public boolean isBlockSet(int x, int y, int z) {
            return blockPalette.get(x, y, z) != 0;
        }

        @Override
        public boolean isBiomeSet(int x, int y, int z) {
            return biomePalette.get(x, y, z) != 0;
        }

        @Override
        public void forEachBlock(BlockConsumer consumer) {
            blockPalette.getAllPresent((x, y, z, id) -> {
                final Block block = Block.fromStateId((short) id);
                consumer.accept(x, y, z, block);
            });
        }

        @Override
        public void forEachBiome(BiomeConsumer consumer) {
            biomePalette.getAllPresent((x, y, z, id) -> {
                final Biome biome = MinecraftServer.getBiomeManager().getById(id);
                consumer.accept(x, y, z, biome);
            });
        }

        @Override
        public void setBiome(int x, int y, int z, Biome biome) {
            biomePalette.set(x, y, z, biome.id());
        }

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            final int id = blockPalette.get(x, y, z);
            return Block.fromStateId((short) id);
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            blockPalette.set(x, y, z, block.stateId());
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            final int id = biomePalette.get(x, y, z);
            return MinecraftServer.getBiomeManager().getById(id);
        }
    }
}
