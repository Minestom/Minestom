package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.instance.light.LightCompute.*;

final class SkyLight implements Light {
    private final Palette blockPalette;

    private byte[] content;
    private byte[] contentPropagation;
    private byte[] contentPropagationSwap;

    private final AtomicBoolean isValidBorders = new AtomicBoolean(true);
    private final AtomicBoolean needsSend = new AtomicBoolean(false);

    private Set<Point> toUpdateSet = new HashSet<>();
    private final Section[] neighborSections = new Section[BlockFace.values().length];
    private boolean fullyLit = false;

    SkyLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
    }

    @Override
    public Set<Point> flip() {
        if (this.contentPropagationSwap != null)
            this.contentPropagation = this.contentPropagationSwap;

        this.contentPropagationSwap = null;

        if (toUpdateSet == null) return Set.of();
        return toUpdateSet;
    }

    static ShortArrayFIFOQueue buildInternalQueue(Chunk c, int sectionY) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        if (c instanceof LightingChunk lc) {
            int[] heightmap = lc.getOcclusionMap();
            int maxY = c.getInstance().getDimensionType().getMinY() + c.getInstance().getDimensionType().getHeight();
            int sectionMaxY = (sectionY + 1) * 16 - 1;
            int sectionMinY = sectionY * 16;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = heightmap[z << 4 | x];

                    for (int y = Math.min(sectionMaxY, maxY); y >= Math.max(height, sectionMinY); y--) {
                        int index = x | (z << 4) | ((y % 16) << 8);
                        lightSources.enqueue((short) (index | (15 << 12)));
                    }
                }
            }
        }

        return lightSources;
    }

    private static Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId((short)palette.get(x, y, z));
    }

    private ShortArrayFIFOQueue buildExternalQueue(Instance instance, Palette blockPalette, Point[] neighbors, byte[] content) {
        ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        for (int i = 0; i < neighbors.length; i++) {
            var face = BlockFace.values()[i];
            Point neighborSection = neighbors[i];
            if (neighborSection == null) continue;

            Section otherSection = neighborSections[face.ordinal()];

            if (otherSection == null) {
                Chunk chunk = instance.getChunk(neighborSection.blockX(), neighborSection.blockZ());
                if (chunk == null) continue;

                otherSection = chunk.getSection(neighborSection.blockY());
                neighborSections[face.ordinal()] = otherSection;
            }

            var otherLight = otherSection.skyLight();

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
                        case NORTH, SOUTH -> getBlock(otherSection.blockPalette(), bx, by, 15 - k);
                        case WEST, EAST -> getBlock(otherSection.blockPalette(), 15 - k, bx, by);
                        default -> getBlock(otherSection.blockPalette(), bx, 15 - k, by);
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
    public Light calculateInternal(Instance instance, int chunkX, int sectionY, int chunkZ) {
        Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            this.toUpdateSet = Set.of();
            return this;
        }
        this.isValidBorders.set(true);

        // Update single section with base lighting changes
        int queueSize = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
        ShortArrayFIFOQueue queue = new ShortArrayFIFOQueue(0);
        if (!fullyLit) {
            queue = buildInternalQueue(chunk, sectionY);
            queueSize = queue.size();
        }

        if (queueSize == SECTION_SIZE * SECTION_SIZE * SECTION_SIZE) {
            this.fullyLit = true;
            this.content = contentFullyLit;
        } else {
            Result result = LightCompute.compute(blockPalette, queue);
            this.content = result.light();
        }

        Set<Point> toUpdate = new HashSet<>();

        // Propagate changes to neighbors and self
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk neighborChunk = instance.getChunk(chunkX + i, chunkZ + j);
                if (neighborChunk == null) continue;

                for (int k = -1; k <= 1; k++) {
                    Vec neighborPos = new Vec(chunkX + i, sectionY + k, chunkZ + j);

                    if (neighborPos.blockY() >= neighborChunk.getMinSection() && neighborPos.blockY() < neighborChunk.getMaxSection()) {
                        if (neighborChunk.getSection(neighborPos.blockY()).skyLight() instanceof SkyLight skyLight) {
                            skyLight.contentPropagation = null;
                            toUpdate.add(new Vec(neighborChunk.getChunkX(), neighborPos.blockY(), neighborChunk.getChunkZ()));
                        }
                    }
                }
            }
        }

        toUpdate.add(new Vec(chunk.getChunkX(), sectionY, chunk.getChunkZ()));
        this.toUpdateSet = toUpdate;

        return this;
    }

    @Override
    public void invalidate() {
        this.needsSend.set(true);
        this.isValidBorders.set(false);
        this.contentPropagation = null;
    }

    @Override
    public boolean requiresUpdate() {
        return !isValidBorders.get();
    }

    @Override
    @ApiStatus.Internal
    public void set(byte[] copyArray) {
        this.content = copyArray.clone();
        this.contentPropagation = this.content;
        this.isValidBorders.set(true);
        this.needsSend.set(false);
    }

    @Override
    public boolean requiresSend() {
        return needsSend.getAndSet(false);
    }

    @Override
    public byte[] array() {
        if (content == null) return new byte[0];
        if (contentPropagation == null) return content;
        var res = bake(contentPropagation, content);
        if (res == emptyContent) return new byte[0];
        return res;
    }

    @Override
    public Light calculateExternal(Instance instance, Chunk chunk, int sectionY) {
        if (!isValidBorders.get()) {
            this.toUpdateSet = Set.of();
            return this;
        }

        Point[] neighbors = Light.getNeighbors(chunk, sectionY);
        Set<Point> toUpdate = new HashSet<>();

        ShortArrayFIFOQueue queue;

        byte[] contentPropagationTemp = contentFullyLit;

        if (!fullyLit) {
            queue = buildExternalQueue(instance, blockPalette, neighbors, content);
            LightCompute.Result result = LightCompute.compute(blockPalette, queue);

            contentPropagationTemp = result.light();
            this.contentPropagationSwap = bake(contentPropagationSwap, contentPropagationTemp);
        } else {
            this.contentPropagationSwap = null;
        }

        // Propagate changes to neighbors and self
        for (int i = 0; i < neighbors.length; i++) {
            var neighbor = neighbors[i];
            if (neighbor == null) continue;

            var face = BlockFace.values()[i];

            if (!Light.compareBorders(content, contentPropagation, contentPropagationTemp, face)) {
                toUpdate.add(neighbor);
            }
        }

        this.toUpdateSet = toUpdate;
        return this;
    }

    private byte[] bake(byte[] content1, byte[] content2) {
        if (content1 == null && content2 == null) return emptyContent;
        if (content1 == emptyContent && content2 == emptyContent) return emptyContent;

        if (content1 == null) return content2;
        if (content2 == null) return content1;

        byte[] lightMax = new byte[LIGHT_LENGTH];
        for (int i = 0; i < content1.length; i++) {
            // Lower
            byte l1 = (byte) (content1[i] & 0x0F);
            byte l2 = (byte) (content2[i] & 0x0F);

            // Upper
            byte u1 = (byte) ((content1[i] >> 4) & 0x0F);
            byte u2 = (byte) ((content2[i] >> 4) & 0x0F);

            byte lower = (byte) Math.max(l1, l2);
            byte upper = (byte) Math.max(u1, u2);

            lightMax[i] = (byte) (lower | (upper << 4));
        }
        return lightMax;
    }

    @Override
    public int getLevel(int x, int y, int z) {
        if (content == null) return 0;
        int index = x | (z << 4) | (y << 8);
        if (contentPropagation == null) return LightCompute.getLight(content, index);
        return Math.max(LightCompute.getLight(contentPropagation, index), LightCompute.getLight(content, index));
    }
}