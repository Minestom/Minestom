package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.instance.light.LightCompute.*;

final class SkyLight implements Light {
    private byte[] content;
    private byte[] contentPropagation;
    private byte[] contentPropagationSwap;

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

    private ShortArrayFIFOQueue buildExternalQueue(Palette blockPalette,
                                                   Point[] neighbors, byte[] content,
                                                   LightLookup lightLookup,
                                                   PaletteLookup paletteLookup) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        for (int i = 0; i < neighbors.length; i++) {
            final BlockFace face = BlockFace.getValues()[i];
            Point neighborSection = neighbors[i];
            if (neighborSection == null) continue;

            Palette otherPalette = paletteLookup.palette(neighborSection.blockX(), neighborSection.blockY(), neighborSection.blockZ());
            Light otherLight = lightLookup.light(neighborSection.blockX(), neighborSection.blockY(), neighborSection.blockZ());

            for (int bx = 0; bx < 16; bx++) {
                for (int by = 0; by < 16; by++) {
                    final int k = switch (face) {
                        case WEST, BOTTOM, NORTH -> 0;
                        case EAST, TOP, SOUTH -> 15;
                    };

                    final byte lightEmission = (byte) Math.max(switch (face) {
                        case NORTH, SOUTH -> (byte) otherLight.getLevel(bx, by, 15 - k);
                        case WEST, EAST -> (byte) otherLight.getLevel(15 - k, bx, by);
                        default -> (byte) otherLight.getLevel(bx, 15 - k, by);
                    } - 1, 0);

                    final int posTo = switch (face) {
                        case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                        case WEST, EAST -> k | (by << 4) | (bx << 8);
                        default -> bx | (by << 4) | (k << 8);
                    };

                    if (content != null) {
                        final int internalEmission = (byte) (Math.max(getLight(content, posTo) - 1, 0));
                        if (lightEmission <= internalEmission) continue;
                    }

                    final Block blockTo = switch (face) {
                        case NORTH, SOUTH -> getBlock(blockPalette, bx, by, k);
                        case WEST, EAST -> getBlock(blockPalette, k, bx, by);
                        default -> getBlock(blockPalette, bx, k, by);
                    };

                    final Block blockFrom = (switch (face) {
                        case NORTH, SOUTH -> getBlock(otherPalette, bx, by, 15 - k);
                        case WEST, EAST -> getBlock(otherPalette, 15 - k, bx, by);
                        default -> getBlock(otherPalette, bx, 15 - k, by);
                    });

                    if (blockTo == null && blockFrom != null) {
                        if (blockFrom.registry().collisionShape().isOccluded(Block.AIR.registry().collisionShape(), face.getOppositeFace()))
                            continue;
                    } else if (blockTo != null && blockFrom == null) {
                        if (Block.AIR.registry().collisionShape().isOccluded(blockTo.registry().collisionShape(), face))
                            continue;
                    } else if (blockTo != null && blockFrom != null) {
                        if (blockFrom.registry().collisionShape().isOccluded(blockTo.registry().collisionShape(), face.getOppositeFace()))
                            continue;
                    }

                    final int index = posTo | (lightEmission << 12);

                    if (lightEmission > 0) {
                        lightSources.enqueue((short) index);
                    }
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
        this.content = copyArray.clone();
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
        if (content == null) return new byte[0];
        if (contentPropagation == null) return content;
        var res = LightCompute.bake(contentPropagation, content);
        if (res == EMPTY_CONTENT) return new byte[0];
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
        int queueSize = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
        ShortArrayFIFOQueue queue = new ShortArrayFIFOQueue(0);
        if (!fullyLit) {
            queue = buildInternalQueue(heightmap, maxY, chunkY);
            queueSize = queue.size();
        }

        if (queueSize == SECTION_SIZE * SECTION_SIZE * SECTION_SIZE) {
            this.fullyLit = true;
            this.content = CONTENT_FULLY_LIT;
        } else {
            this.content = LightCompute.compute(blockPalette, queue);
        }

        // Propagate changes to neighbors and self
        Set<Point> toUpdate = new HashSet<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    final int neighborX = chunkX + i;
                    final int neighborY = chunkY + j;
                    final int neighborZ = chunkZ + k;
                    if (!(lightLookup.light(neighborX, neighborY, neighborZ) instanceof SkyLight skyLight))
                        continue;
                    skyLight.contentPropagation = null;
                    toUpdate.add(new Vec(neighborX, neighborY, neighborZ));
                }
            }
        }
        toUpdate.add(new Vec(chunkX, chunkY, chunkZ));
        return toUpdate;
    }

    @Override
    public Set<Point> calculateExternal(Palette blockPalette,
                                        Point[] neighbors,
                                        LightLookup lightLookup,
                                        PaletteLookup paletteLookup) {
        if (!isValidBorders) {
            return Set.of();
        }
        byte[] contentPropagationTemp = CONTENT_FULLY_LIT;
        if (!fullyLit) {
            ShortArrayFIFOQueue queue = buildExternalQueue(blockPalette, neighbors, content, lightLookup, paletteLookup);
            contentPropagationTemp = LightCompute.compute(blockPalette, queue);
            this.contentPropagationSwap = LightCompute.bake(contentPropagationSwap, contentPropagationTemp);
        } else {
            this.contentPropagationSwap = null;
        }
        // Propagate changes to neighbors and self
        Set<Point> toUpdate = new HashSet<>();
        for (int i = 0; i < neighbors.length; i++) {
            final Point neighbor = neighbors[i];
            if (neighbor == null) continue;
            final BlockFace face = BlockFace.getValues()[i];
            if (!LightCompute.compareBorders(content, contentPropagation, contentPropagationTemp, face)) {
                toUpdate.add(neighbor);
            }
        }
        return toUpdate;
    }
}
