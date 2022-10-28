package net.minestom.server.instance;

import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

public final class PaletteSectionData implements Writeable {
    private final Palette blockPalette;
    private final Palette biomePalette;
    private byte[] skyLight;
    private byte[] blockLight;

    private final Section section;

    // Changed used for cache invalidation
    private boolean changed;

    private PaletteSectionData(Palette blockPalette, Palette biomePalette,
                               byte[] skyLight, byte[] blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
        this.section = new SectionView();
        change();
    }

    public PaletteSectionData() {
        this(Palette.blocks(), Palette.biomes(),
                new byte[0], new byte[0]);
    }

    public PaletteSectionData(Section copy) {
        this();
        // Copy the section
        sectionView().copy(copy);
    }

    private ByteList getSkyLight() {
        return ByteList.of(skyLight);
    }

    private void setSkyLight(ByteList skyLight) {
        this.skyLight = skyLight.toByteArray();
        change();
    }

    private ByteList getBlockLight() {
        return ByteList.of(blockLight);
    }

    private void setBlockLight(ByteList blockLight) {
        this.blockLight = blockLight.toByteArray();
        change();
    }

    private void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.skyLight = new byte[0];
        this.blockLight = new byte[0];
        change();
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

    private void change() {
        this.changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }

    public @NotNull Section sectionView() {
        return section;
    }

    public void acknowledgeChanges() {
        this.changed = false;
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
            x = ChunkUtils.toSectionRelativeCoordinate(x);
            y = ChunkUtils.toSectionRelativeCoordinate(y);
            z = ChunkUtils.toSectionRelativeCoordinate(z);
            return blockPalette.get(x, y, z) != 0;
        }

        @Override
        public boolean isBiomeSet(int x, int y, int z) {
            x = ChunkUtils.toSectionRelativeCoordinate(x) / 4;
            y = ChunkUtils.toSectionRelativeCoordinate(y) / 4;
            z = ChunkUtils.toSectionRelativeCoordinate(z) / 4;
            return biomePalette.get(x, y, z) != 0;
        }

        @Override
        public void forEachBlock(BlockConsumer consumer) {
            blockPalette.getAllPresent((x, y, z, id) -> {
                final Block block = Block.fromStateId((short) id);
                if (block == null) throw new IllegalStateException("Block with id " + id + " is not registered");
                consumer.accept(x, y, z, block);
            });
        }

        @Override
        public void forEachBiome(BiomeConsumer consumer) {
            biomePalette.getAllPresent((x, y, z, id) -> {
                final Biome biome = MinecraftServer.getBiomeManager().getById(id);
                if (biome == null) throw new IllegalStateException("Biome with id " + id + " is not registered");
                consumer.accept(x, y, z, biome);
            });
        }

        @Override
        public void setBiome(int x, int y, int z, Biome biome) {
            x = ChunkUtils.toSectionRelativeCoordinate(x) / 4;
            y = ChunkUtils.toSectionRelativeCoordinate(y) / 4;
            z = ChunkUtils.toSectionRelativeCoordinate(z) / 4;
            biomePalette.set(x, y, z, biome.id());
            change();
        }

        @Override
        public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
            x = ChunkUtils.toSectionRelativeCoordinate(x);
            y = ChunkUtils.toSectionRelativeCoordinate(y);
            z = ChunkUtils.toSectionRelativeCoordinate(z);
            final int id = blockPalette.get(x, y, z);
            return Block.fromStateId((short) id);
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            x = ChunkUtils.toSectionRelativeCoordinate(x);
            y = ChunkUtils.toSectionRelativeCoordinate(y);
            z = ChunkUtils.toSectionRelativeCoordinate(z);
            blockPalette.set(x, y, z, block.stateId());
            change();
        }

        @Override
        public @NotNull Biome getBiome(int x, int y, int z) {
            x = ChunkUtils.toSectionRelativeCoordinate(x) / 4;
            y = ChunkUtils.toSectionRelativeCoordinate(y) / 4;
            z = ChunkUtils.toSectionRelativeCoordinate(z) / 4;
            final int id = biomePalette.get(x, y, z);
            return MinecraftServer.getBiomeManager().getById(id);
        }
    }
}
