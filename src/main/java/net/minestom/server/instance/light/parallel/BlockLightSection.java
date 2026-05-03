package net.minestom.server.instance.light.parallel;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.light.LightCompute;
import net.minestom.server.instance.light.parallel.ParallelLightSection.LightData;
import net.minestom.server.instance.light.parallel.ParallelLightSection.LightUpdateResult;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.VisibleForTesting;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;
import static net.minestom.server.instance.light.parallel.ParallelLightSection.computeExternal;

final class BlockLightSection {
    private final ParallelLightSection section;

    public BlockLightSection(ParallelLightSection section) {
        this.section = section;
    }

    LightUpdateResult<byte[]> relightBlockLightExternal() {
        var version = section.getNextBlockLightExternalVersion();
        var externalLight = computeExternal(section, ParallelLightSection::getBlockLight, s -> s.getBlockLightInternal().data());
        var newData = new LightData<>(externalLight, version);
        return section.updateBlockLightExternal(newData);
    }

    private LightData<byte[]> prepareBlockLightInternal(int version) {
        try {
            // We do not have internal sources, we are a bridge section
            if (section.chunkSection == null) return new LightData<>(LightCompute.EMPTY_CONTENT, version);
            var blockPalette = section.getBlockData().data().blockPalette();
            return new LightData<>(computeBlockLight(blockPalette), version);
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(t);
            return new LightData<>(LightCompute.EMPTY_CONTENT, version);
        }
    }

    LightUpdateResult<byte[]> relightBlockLightInternal() {
        var version = section.getNextBlockLightInternalVersion();
        var lightData = prepareBlockLightInternal(version);
        return section.updateBlockLightInternal(lightData);
    }

    @VisibleForTesting
    public static byte[] computeBlockLight(Palette palette) {
        return LightCompute.compute(palette, getBlockLightInternalSources(palette));
    }

    /**
     * Collect all the block light sources from the given palette into a FIFO queue.
     */
    public static ShortArrayFIFOQueue getBlockLightInternalSources(Palette blockPalette) {
        if (blockPalette.isEmpty()) return new ShortArrayFIFOQueue(0); // Avoid state id lookup for air

        int singleValue = blockPalette.singleValue();
        if (singleValue != -1) {
            Block block = Block.fromStateId(singleValue);
            assert block != null;
            int lightEmission = block.registry().lightEmission();
            if (lightEmission <= 0) return new ShortArrayFIFOQueue(0);
            ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue(SECTION_BLOCK_COUNT);
            final int prefix = lightEmission << 12;
            for (int index = 0; index < SECTION_BLOCK_COUNT; index++) {
                lightSources.enqueue((short) (index | prefix));
            }
            return lightSources;
        } else {
            ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();
            // Apply section light
            blockPalette.getAllPresent((x, y, z, stateId) -> {
                final Block block = Block.fromStateId(stateId);
                assert block != null;
                final int lightEmission = block.registry().lightEmission();
                if (lightEmission <= 0) return;
                final int index = x | (z << 4) | (y << 8);
                lightSources.enqueue((short) (index | (lightEmission << 12)));
            });
            return lightSources;
        }
    }
}
