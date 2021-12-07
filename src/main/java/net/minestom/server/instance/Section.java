package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.snapshot.SectionSnapshot;
import net.minestom.server.snapshot.Snapshotable;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public final class Section implements Snapshotable, Writeable {
    private final Chunk chunk;
    private final int index;
    private Palette blockPalette;
    private Palette biomePalette;
    private byte[] skyLight;
    private byte[] blockLight;

    private Section(Chunk chunk, int index,
                    Palette blockPalette, Palette biomePalette,
                    byte[] skyLight, byte[] blockLight) {
        this.chunk = chunk;
        this.index = index;
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    public Section(Chunk chunk, int index) {
        this(chunk, index, Palette.blocks(), Palette.biomes(),
                new byte[0], new byte[0]);
    }

    public Palette blockPalette() {
        return blockPalette;
    }

    public Palette biomePalette() {
        return biomePalette;
    }

    public byte[] getSkyLight() {
        return skyLight;
    }

    public void setSkyLight(byte[] skyLight) {
        this.skyLight = skyLight;
    }

    public byte[] getBlockLight() {
        return blockLight;
    }

    public void setBlockLight(byte[] blockLight) {
        this.blockLight = blockLight;
    }

    public void clear() {
        this.blockPalette = Palette.blocks();
        this.biomePalette = Palette.biomes();
        this.skyLight = new byte[0];
        this.blockLight = new byte[0];
    }

    public int index() {
        return index;
    }

    @Override
    public @NotNull Section clone() {
        return new Section(chunk, index, blockPalette.clone(), biomePalette.clone(),
                skyLight.clone(), blockLight.clone());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort((short) blockPalette.size());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }

    private static final AtomicReferenceFieldUpdater<Section, SectionSnapshot> SNAPSHOT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Section.class, SectionSnapshot.class, "snapshot");
    private volatile SectionSnapshot snapshot;

    @Override
    public @NotNull SectionSnapshot snapshot() {
        return SNAPSHOT_UPDATER.updateAndGet(this,
                snap -> snap != null ? snap : generateSnapshot());
    }

    @Override
    public @NotNull SectionSnapshot updatedSnapshot() {
        return SNAPSHOT_UPDATER.updateAndGet(this, s -> s != null ? s : generateSnapshot());
    }

    @Override
    public void triggerSnapshotChange(Snapshotable snapshotable) {
        this.chunk.triggerSnapshotChange(this);
    }

    private synchronized SectionSnapshot generateSnapshot() {
        var clone = clone();
        return new SectionSnapshot() {
            @Override
            public int index() {
                return clone.index;
            }

            @Override
            public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
                return Block.fromStateId((short) clone.blockPalette().get(x, y, z)); // TODO retrieve handler/nbt
            }

            @Override
            public @NotNull Biome getBiome(int x, int y, int z) {
                return MinecraftServer.getBiomeManager().getById(clone.biomePalette().get(x, y, z));
            }
        };
    }
}
