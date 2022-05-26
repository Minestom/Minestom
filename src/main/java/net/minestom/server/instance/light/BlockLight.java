package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static net.minestom.server.instance.light.BlockLightCompute.*;

final class BlockLight implements Light {
    private final Palette blockPalette;

    private volatile byte[] content;
    private volatile byte[] contentPropagation;

    private volatile byte[][] borders;
    private volatile byte[][] bordersPropagation;

    BlockLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
        this.contentPropagation = new byte[16 * 16 * 16 / 2];
        this.content = new byte[16 * 16 * 16 / 2];

        this.bordersPropagation = new byte[FACES.length][];
        Arrays.setAll(bordersPropagation, i -> new byte[SIDE_LENGTH]);

        this.borders = new byte[FACES.length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);
    }

    public static IntArrayFIFOQueue buildInternalQueue(Palette blockPalette, Block[] blocks) {
        IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();
        // Apply section light
        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();

            final int index = x | (z << 4) | (y << 8);
            blocks[index] = block;
            if (lightEmission > 0) {
                lightSources.enqueue(index | (lightEmission << 12));
            }
        });
        return lightSources;
    }

    public static IntArrayFIFOQueue buildExternalQueue(Map<BlockFace, Instance.SectionLocation> neighbors, Chunk chunk, int sectionY) {
        IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();
        for (BlockFace face : BlockFace.values()) {
            Instance.SectionLocation neighborSection = neighbors.get(face);
            if (neighborSection == null) return lightSources;
            byte[] neighborFace = Instance.getSection(neighborSection.chunk(), neighborSection.sectionY()).blockLight().getBorderPropagation(face.getOppositeFace());

            for (int bx = 0; bx < 16; bx++) {
                for (int by = 0; by < 16; by++) {
                    final int k = switch (face) {
                        case WEST, BOTTOM, NORTH -> 0;
                        case EAST, TOP, SOUTH -> 15;
                    };

                    final Point posTo = (switch (face) {
                        case NORTH, SOUTH ->
                                new Pos(bx + chunk.getChunkX() * 16, by + sectionY * 16, k + chunk.getChunkZ() * 16);
                        case WEST, EAST ->
                                new Pos(k + chunk.getChunkX() * 16, bx + sectionY * 16, by + chunk.getChunkZ() * 16);
                        default -> new Pos(bx + chunk.getChunkX() * 16, k + sectionY * 16, by + chunk.getChunkZ() * 16);
                    });

                    final Point posFrom = (switch (face) {
                        case NORTH, SOUTH ->
                                new Pos(bx + neighborSection.chunk().getChunkX() * 16, by + neighborSection.sectionY() * 16, 15 - k + neighborSection.chunk().getChunkZ() * 16);
                        case WEST, EAST ->
                                new Pos(15 - k + neighborSection.chunk().getChunkX() * 16, bx + neighborSection.sectionY() * 16, by + neighborSection.chunk().getChunkZ() * 16);
                        default ->
                                new Pos(bx + neighborSection.chunk().getChunkX() * 16, 15 - k + neighborSection.sectionY() * 16, by + neighborSection.chunk().getChunkZ() * 16);
                    });

                    Instance instance = chunk.getInstance();
                    Block blockTo = instance.getBlock(posTo);
                    Block blockFrom = instance.getBlock(posFrom);

                    if (blockFrom.registry().collisionShape().isOccluded(blockTo.registry().collisionShape(), face.getOppositeFace()))
                        continue;

                    final int borderIndex = bx * SECTION_SIZE + by;
                    byte lightEmission = neighborFace[borderIndex];

                    final int index = switch (face) {
                        case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                        case WEST, EAST -> k | (by << 4) | (bx << 8);
                        default -> bx | (by << 4) | (k << 8);
                    } | (lightEmission << 12);

                    if (lightEmission > 0) {
                        lightSources.enqueue(index);
                    }
                }
            }
        }
        return lightSources;
    }

    @Override
    public void copyFrom(byte @NotNull [] array) {
        this.content = array.clone();
    }

    @Override
    public Stream<Instance.SectionLocation> calculateInternal(Instance instance, int chunkX, int sectionY, int chunkZ) {
        System.out.println("Calculating internal light for chunk " + chunkX + " " + " " + sectionY + " " + chunkZ);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Pos neighborPos = new Pos(chunkX, sectionY, chunkZ).add(x, y, z);

                    Chunk neighborChunk = instance.getChunk(neighborPos.blockX(), neighborPos.blockZ());
                    if (neighborChunk == null) continue;

                    if (neighborPos.blockY() >= neighborChunk.getMinSection()  && neighborPos.blockY() < neighborChunk.getMaxSection()) {
                        neighborChunk.getSection(neighborPos.blockY()).blockLight().invalidatePropagation();
                        neighborChunk.invalidate();
                    }
                }
            }
        }

        Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (chunk == null) return Stream.empty();

        Set<Instance.SectionLocation> toUpdate = new HashSet<>();

        // Update single section with base lighting changes
        Block[] blocks = new Block[SECTION_SIZE * SECTION_SIZE * SECTION_SIZE];
        IntArrayFIFOQueue queue = buildInternalQueue(blockPalette, blocks);

        Result result = BlockLightCompute.compute(blocks, queue);
        this.content = result.light();
        this.borders = new byte[FACES.length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);

        var neighbors = instance.getNeighbors(chunk, sectionY);

        // Propagate changes to neighbors and self
        for (BlockFace face : BlockFace.values()) {
            Instance.SectionLocation neighbor = neighbors.get(face);
            if (neighbor == null) continue;
            toUpdate.add(neighbor);
        }

        this.borders = result.borders();
        return toUpdate.stream();
    }

    @Override
    public byte[] array() {
        return bake(contentPropagation, content);
    }

    private boolean compareBorders(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > b[i]) return false;
        }
        return true;
    }

    private static Block[] blocks(Palette blockPalette) {
        Block[] blocks = new Block[SECTION_SIZE * SECTION_SIZE * SECTION_SIZE];
        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final int index = x | (z << 4) | (y << 8);
            blocks[index] = block;
        });
        return blocks;
    }

    @Override
    public Stream<Instance.SectionLocation> calculateExternal(Instance instance, Chunk chunk, int sectionY) {
        System.out.println("Calculating external light for chunk " + chunk + " " + sectionY);

        var neighbors = instance.getNeighbors(chunk, sectionY);
        Set<Instance.SectionLocation> needsUpdate = new HashSet<>();

        IntArrayFIFOQueue queue = buildExternalQueue(neighbors, chunk, sectionY);
        BlockLightCompute.Result result = BlockLightCompute.compute(blocks(blockPalette), queue);

        byte[] contentPropagationTemp = result.light();
        byte[][] borderTemp = result.borders();

        synchronized (this) {
            this.contentPropagation = bake(contentPropagation, contentPropagationTemp);
        }

        // Propagate changes to neighbors and self
        for (var entry : neighbors.entrySet()) {
            var neighbor = entry.getValue();
            var face = entry.getKey();

            byte[] next = result.borders()[face.ordinal()];
            byte[] current = combineBorders(bordersPropagation[face.ordinal()], this.borders[face.ordinal()]);
            // var current = bordersPropagation[face.ordinal()];

            if (!compareBorders(next, current)) {
                needsUpdate.add(neighbor);
            }
        }

        synchronized (this) {
            this.bordersPropagation = combineBorders(bordersPropagation, borderTemp);
        }

        return needsUpdate.stream();
    }

    private byte[][] combineBorders(byte[][] b1, byte[][] b2) {
        byte[][] newBorder = new byte[FACES.length][];
        Arrays.setAll(newBorder, i -> new byte[SIDE_LENGTH]);
        for (int i = 0; i < FACES.length; i++) {
            newBorder[i] = combineBorders(b1[i], b2[i]);
        }
        return newBorder;
    }

    private byte[] bake(byte[] content1, byte[] content2) {
        byte[] lightMax = new byte[16 * 16 * 16 / 2];
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
    public byte[] getBorderPropagation(BlockFace face) {
        return combineBorders(bordersPropagation[face.ordinal()], borders[face.ordinal()]);
    }

    @Override
    public void invalidatePropagation() {
        this.contentPropagation = new byte[LIGHT_LENGTH];
        this.bordersPropagation = new byte[FACES.length][];
        Arrays.setAll(bordersPropagation, i -> new byte[SIDE_LENGTH]);
    }

    @Override
    public int getLevel(int x, int y, int z) {
        var array = array();
        int index = x | (z << 4) | (y << 8);
        return BlockLightCompute.getLight(array, index);
    }

    private byte[] combineBorders(byte[] b1, byte[] b2) {
        byte[] newBorder = new byte[SIDE_LENGTH];
        for (int i = 0; i < newBorder.length; i++) {
            var previous = b2[i];
            var current = b1[i];
            newBorder[i] = (byte) Math.max(previous, current);
        }
        return newBorder;
    }
}
