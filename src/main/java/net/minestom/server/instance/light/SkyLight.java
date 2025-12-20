package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.SectionVec;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;
import static net.minestom.server.instance.light.LightCompute.*;

final class SkyLight implements Light {
    private byte @Nullable [] content;
    private byte @Nullable [] contentPropagation;
    private byte @Nullable [] contentPropagationSwap;

    private volatile boolean isValidBorders = true;
    private final AtomicBoolean needsSend = new AtomicBoolean(false);

    private boolean fullyLit = false;

    @Override
    public void flip() {
        if (this.contentPropagationSwap != null)
            this.contentPropagation = this.contentPropagationSwap;
        this.contentPropagationSwap = null;
    }

    static ShortArrayFIFOQueue buildInternalQueue(int[] heightmap, int maxY, int sectionY) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();
        final int sectionMaxY = (sectionY + 1) * 16 - 1;
        final int sectionMinY = sectionY * 16;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int height = heightmap[z << 4 | x];
                for (int y = Math.min(sectionMaxY, maxY); y >= Math.max(height, sectionMinY); y--) {
                    final int index = x | (z << 4) | ((y % 16) << 8);
                    lightSources.enqueue((short) (index | (15 << 12)));
                }
            }
        }
        return lightSources;
    }

    @Override
    public void invalidate() {
        this.needsSend.set(true);
        this.isValidBorders = false;
        this.contentPropagation = null;
    }

    @Override
    public boolean requiresUpdate() {
        return !isValidBorders;
    }

    @Override
    @ApiStatus.Internal
    public void set(byte[] copyArray) {
        this.content = lazyArray(copyArray);
        this.contentPropagation = this.content;
        this.isValidBorders = true;
        this.needsSend.set(true);
    }

    @Override
    public boolean requiresSend() {
        return needsSend.getAndSet(false);
    }

    @Override
    public byte[] array() {
        if (content == null) return UNSET_CONTENT;
        if (contentPropagation == null) return content;
        var res = LightCompute.bake(contentPropagation, content);
        if (res == EMPTY_CONTENT) return UNSET_CONTENT;
        return res;
    }

    @Override
    public int getLevel(int x, int y, int z) {
        if (content == null) return 0;
        int index = x | (z << 4) | (y << 8);
        if (contentPropagation == null) return LightCompute.getLight(content, index);
        return Math.max(LightCompute.getLight(contentPropagation, index), LightCompute.getLight(content, index));
    }

    @Override
    public Set<SectionVec> calculateInternal(Palette blockPalette, int chunkX, int chunkY, int chunkZ, int[] heightmap, int maxY, LightLookup lightLookup) {
        this.isValidBorders = true;

        // Update single section with base lighting changes
        int queueSize = SECTION_BLOCK_COUNT;
        ShortArrayFIFOQueue queue = new ShortArrayFIFOQueue(0);
        if (!fullyLit) {
            queue = buildInternalQueue(heightmap, maxY, chunkY);
            queueSize = queue.size();
        }

        if (queueSize == SECTION_BLOCK_COUNT) {
            this.fullyLit = true;
            this.content = CONTENT_FULLY_LIT;
        } else {
            this.content = LightCompute.compute(blockPalette, queue);
        }

        // Propagate changes to neighbors and self
        Set<SectionVec> toUpdate = new HashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    final int neighborX = chunkX + x;
                    final int neighborY = chunkY + y;
                    final int neighborZ = chunkZ + z;
                    if (!(lightLookup.light(neighborX, neighborY, neighborZ) instanceof SkyLight skyLight))
                        continue;
                    skyLight.contentPropagation = null;
                    toUpdate.add(new SectionVec(neighborX, neighborY, neighborZ));
                }
            }
        }
        toUpdate.add(new SectionVec(chunkX, chunkY, chunkZ));
        return toUpdate;
    }

    @Override
    public Set<SectionVec> calculateExternal(Palette blockPalette,
                                             @Nullable SectionVec[] neighbors,
                                             LightLookup lightLookup,
                                             PaletteLookup paletteLookup) {
        if (!isValidBorders) return Set.of();
        byte[] contentPropagationTemp = CONTENT_FULLY_LIT;
        if (!fullyLit) {
            ShortArrayFIFOQueue queue = buildExternalQueue(blockPalette, neighbors, content, lightLookup, paletteLookup);
            contentPropagationTemp = LightCompute.compute(blockPalette, queue);
            this.contentPropagationSwap = LightCompute.bake(contentPropagationSwap, contentPropagationTemp);
        } else {
            this.contentPropagationSwap = null;
        }
        // Propagate changes to neighbors and self
        Set<SectionVec> toUpdate = new HashSet<>();
        for (int i = 0; i < neighbors.length; i++) {
            final SectionVec neighbor = neighbors[i];
            if (neighbor == null) continue;
            final BlockFace face = FACES[i];
            if (!LightCompute.compareBorders(content, contentPropagation, contentPropagationTemp, face)) {
                toUpdate.add(neighbor);
            }
        }
        return toUpdate;
    }
}
