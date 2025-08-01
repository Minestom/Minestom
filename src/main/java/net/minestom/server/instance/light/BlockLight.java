package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.instance.light.LightCompute.*;

final class BlockLight implements Light {
    private byte[] content;
    private byte[] contentPropagation;
    private byte[] contentPropagationSwap;

    private volatile boolean isValidBorders = true;
    private final AtomicBoolean needsSend = new AtomicBoolean(false);

    @Override
    public void flip() {
        if (this.contentPropagationSwap != null)
            this.contentPropagation = this.contentPropagationSwap;
        this.contentPropagationSwap = null;
    }

    static ShortArrayFIFOQueue buildInternalQueue(Palette blockPalette) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();
        // Apply section light
        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId(stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();

            final int index = x | (z << 4) | (y << 8);
            if (lightEmission > 0) {
                lightSources.enqueue((short) (index | (lightEmission << 12)));
            }
        });
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
    public Set<Point> calculateInternal(Palette blockPalette,
                                        int chunkX, int chunkY, int chunkZ,
                                        int[] heightmap, int maxY,
                                        LightLookup lightLookup) {
        this.isValidBorders = true;
        // Update single section with base lighting changes
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
        return Set.of(new BlockVec(chunkX, chunkY, chunkZ));
    }

    @Override
    public Set<Point> calculateExternal(Palette blockPalette,
                                        Point[] neighbors,
                                        LightLookup lightLookup,
                                        PaletteLookup paletteLookup) {
        if (!isValidBorders) return Set.of();
        ShortArrayFIFOQueue queue = buildExternalQueue(blockPalette, neighbors, content, lightLookup, paletteLookup);
        final byte[] contentPropagationTemp = LightCompute.compute(blockPalette, queue);
        this.contentPropagationSwap = LightCompute.bake(contentPropagationSwap, contentPropagationTemp);
        // Propagate changes to neighbors and self
        Set<Point> toUpdate = new HashSet<>();
        for (int i = 0; i < neighbors.length; i++) {
            final Point neighbor = neighbors[i];
            if (neighbor == null) continue;
            final BlockFace face = FACES[i];
            if (!LightCompute.compareBorders(content, contentPropagation, contentPropagationTemp, face)) {
                toUpdate.add(neighbor);
            }
        }
        return toUpdate;
    }
}
