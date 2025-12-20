package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.SectionVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;
import static net.minestom.server.instance.light.LightCompute.*;

final class BlockLight implements Light {
    private byte @Nullable [] content;
    private byte @Nullable [] contentPropagation;
    private byte @Nullable [] contentPropagationSwap;

    private volatile boolean isValidBorders = true;
    private final AtomicBoolean needsSend = new AtomicBoolean(false);

    @Override
    public void flip() {
        if (this.contentPropagationSwap != null)
            this.contentPropagation = this.contentPropagationSwap;
        this.contentPropagationSwap = null;
    }

    static ShortArrayFIFOQueue buildInternalQueue(Palette blockPalette) {
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
    public Set<SectionVec> calculateInternal(Palette blockPalette,
                                             int chunkX, int chunkY, int chunkZ,
                                             int[] heightmap, int maxY,
                                             LightLookup lightLookup) {
        this.isValidBorders = true;
        // Update a single section with base lighting changes
        ShortArrayFIFOQueue queue = buildInternalQueue(blockPalette);
        this.content = LightCompute.compute(blockPalette, queue);
        // Propagate changes to neighbors and self
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    final int neighborX = chunkX + i;
                    final int neighborY = chunkY + j;
                    final int neighborZ = chunkZ + k;
                    if (!(lightLookup.light(neighborX, neighborY, neighborZ) instanceof BlockLight blockLight))
                        continue;
                    blockLight.contentPropagation = null;
                }
            }
        }
        return Set.of(new SectionVec(chunkX, chunkY, chunkZ));
    }

    @Override
    public Set<SectionVec> calculateExternal(Palette blockPalette,
                                             @Nullable SectionVec[] neighbors,
                                             LightLookup lightLookup,
                                             PaletteLookup paletteLookup) {
        if (!isValidBorders) return Set.of();
        // Calculate incoming emissions from neighbors
        ShortArrayFIFOQueue queue = buildExternalQueue(blockPalette, neighbors, content, lightLookup, paletteLookup);
        // Compute the light levels based on the emissions from neighbors
        final byte[] contentPropagationTemp = LightCompute.compute(blockPalette, queue);
        this.contentPropagationSwap = LightCompute.bake(contentPropagationSwap, contentPropagationTemp);
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
