package net.minestom.server.instance.light.snapshot;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.light.LightCompute;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;

class BlockLight {
    private static final Palette STONE = Palette.blocks();
    private final SnapshotLightSection section;
    private final UpdatableContent<byte[]> light = new UpdatableContent<>(LightCompute.EMPTY_CONTENT);

    static {
        STONE.fill(Block.STONE.stateId());
    }

    public BlockLight(SnapshotLightSection section) {
        this.section = section;
    }

    byte[] get() {
        return light.data();
    }

    void relightSync(CalculationContext context) {
        var sources = getLightSources(context);
        var queue = sources.queue();
        var blockPalettes = sources.blockPalettes();
        var light = LightCompute.compute3x3(blockPalettes, queue);
        var changed = this.light.update(light, context.version());
        if (changed) {
            section.scheduleResendBlock();
        }
    }

    private static Sources getLightSources(CalculationContext context) {
        var queue = new IntArrayFIFOQueue(0);
        var blockPalettes = new Palette[27];
        fill(blockPalettes, queue, context.originChunk());
        for (var value : context.neighbors().values()) {
            fill(blockPalettes, queue, value);
        }

        for (int i = 0; i < blockPalettes.length; i++) {
            if (blockPalettes[i] == null) {
                // We treat all unknown/invalid chunks/sections as completely opaque.
                blockPalettes[i] = STONE;
            }
        }
        return new Sources(blockPalettes, queue);
    }

    private static class UpdatableContent<T> {
        private int version = 0;
        private T data;

        public UpdatableContent(T data) {
            this.data = data;
        }

        public synchronized T data() {
            return data;
        }

        public synchronized boolean update(T newData, int version) {
            if (version < this.version) return false;
            this.version = version;
            this.data = newData;
            return true;
        }
    }

    private record Sources(Palette[] blockPalettes, IntArrayFIFOQueue queue) {
    }

    private static void fill(Palette[] blockPalettes, IntArrayFIFOQueue queue, CalculationContext.@Nullable ChunkContext chunkContext) {
        if (chunkContext == null) return;
        var neighbor = chunkContext.neighbor();
        var x = neighbor == null ? 0 : neighbor.x();
        var z = neighbor == null ? 0 : neighbor.z();
        fill(blockPalettes, queue, x, -1, z, chunkContext.lower());
        fill(blockPalettes, queue, x, 0, z, chunkContext.middle());
        fill(blockPalettes, queue, x, 1, z, chunkContext.upper());
    }

    private static void fill(Palette[] blockPalettes, IntArrayFIFOQueue queue, int relSectionX, int relSectionY, int relSectionZ, @Nullable SectionSnapshot snapshot) {
        if (snapshot == null) return;
        int sectionIdx = LightCompute.sectionIdx3x3((relSectionX + 1) << 4, (relSectionY + 1) << 4, (relSectionZ + 1) << 4);
        Palette blockPalette = snapshot.blockPalette();
        blockPalettes[sectionIdx] = blockPalette;
        if (blockPalette.isEmpty()) return; // Avoid state id lookup for air

        int sectionPos = (relSectionY + 1) << 4 | (relSectionZ + 1) << 2 | (relSectionX + 1);
        int singleValue = blockPalette.singleValue();
        if (singleValue != -1) {
            Block block = Block.fromStateId(singleValue);
            assert block != null;
            int lightEmission = block.registry().lightEmission();
            if (lightEmission <= 0) return;
            final int prefix = sectionPos << 21 | sectionIdx << 16 | lightEmission << 12;
            for (int index = 0; index < SECTION_BLOCK_COUNT; index++) {
                int val = index | prefix;
                queue.enqueue(val);
            }
        } else {
            final int prefix = sectionPos << 21 | sectionIdx << 16;
            // Apply section light
            blockPalette.getAllPresent((x, y, z, stateId) -> {
                final Block block = Block.fromStateId(stateId);
                assert block != null;
                final int lightEmission = block.registry().lightEmission();
                if (lightEmission <= 0) return;
                final int index = x | (z << 4) | (y << 8);
                queue.enqueue(prefix | (lightEmission << 12) | index);
            });
        }
    }
}
