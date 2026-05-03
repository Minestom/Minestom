package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;

public final class LightCompute {
    static final Direction[] DIRECTIONS = Direction.values();
    static final int LIGHT_LENGTH = SECTION_BLOCK_COUNT / 2;
    static final int SECTION_SIZE = 16;

    public static final byte[] EMPTY_CONTENT = new byte[LIGHT_LENGTH];
    public static final byte[] CONTENT_FULLY_LIT = new byte[LIGHT_LENGTH];

    static {
        Arrays.fill(CONTENT_FULLY_LIT, (byte) -1);
    }

    private static final int SECTION_SIZE_3 = SECTION_SIZE * 3;

    /**
     * Computes light in 3x3 sections for the center section
     * <p>
     * Takes queue of lights positions and spreads light from this positions in 3d using Breadth-first search
     *
     * @param blockPalettes blocks placed in sections
     * @param lightPre      ints queue in format: [5 bits 0][2 bit sectionY][2 bit sectionZ][2 bit sectionX][5 bit sectionIdx][4bit light level][4bit y][4bit z][4bit x]
     * @return lighting wrapped in Result
     */
    static byte[] compute3x3(Palette[] blockPalettes, IntArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) return EMPTY_CONTENT;

        final byte[][] lightArrays = new byte[27][LIGHT_LENGTH];

        final IntArrayFIFOQueue lightSources = new IntArrayFIFOQueue();

        while (!lightPre.isEmpty()) {
            final int index = lightPre.dequeueInt();

            final int sectionIdx = (index >> 16) & 0x1F;
            final int newLightLevel = (index >> 12) & 15;
            final int newIndex = index & 0xFFF;

            final int oldLightLevel = getLight(lightArrays[sectionIdx], newIndex);

            if (oldLightLevel < newLightLevel) {
                placeLight(lightArrays[sectionIdx], newIndex, newLightLevel);
                lightSources.enqueue(index);
            }
        }

        while (!lightSources.isEmpty()) {
            final int index = lightSources.dequeueInt();
            final int sectionIdx = (index >> 16) & 0x1F;
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int lightLevel = (index >> 12) & 15;

            final int sectionX = ((index >> 21) & 0x3) << 4;
            final int sectionZ = ((index >> 23) & 0x3) << 4;
            final int sectionY = ((index >> 25) & 0x3) << 4;

            for (Direction direction : DIRECTIONS) {
                final int xO = x + direction.normalX();
                final int yO = y + direction.normalY();
                final int zO = z + direction.normalZ();
                final int absXO = sectionX + xO;
                final int absYO = sectionY + yO;
                final int absZO = sectionZ + zO;

                // Handler border
                if (absXO < 0 || absXO >= SECTION_SIZE_3 || absYO < 0 || absYO >= SECTION_SIZE_3 || absZO < 0 || absZO >= SECTION_SIZE_3) {
                    continue;
                }
                final int sectionIdxO = sectionIdx3x3(sectionX + xO, sectionY + yO, sectionZ + zO);

                // Section
                final int newIndex = index3x3(xO, yO, zO);
                final Block targetBlock = getBlock(blockPalettes[sectionIdxO], xO & 0xF, yO & 0xF, zO & 0xF);
                final int opacity = targetBlock.registry().lightBlocked();
                final byte newLightLevel = (byte) Math.max(lightLevel - Math.max(opacity, 1), 0);

                if (getLight(lightArrays[sectionIdxO], newIndex) < newLightLevel) {
                    final Block currentBlock = getBlock(blockPalettes[sectionIdx], x, y, z);
                    final Block propagatedBlock = getBlock(blockPalettes[sectionIdxO], xO & 0xF, yO & 0xF, zO & 0xF);

                    final Shape currentShape = currentBlock.registry().occlusionShape();
                    final Shape propagatedShape = propagatedBlock.registry().occlusionShape();

                    if (sectionIdxO != sectionIdx && sectionIdxO == sectionIdx3x3(16, 16, 16))
                        System.out.println("Cross boundary");

                    final boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentShape.isOccluded(propagatedShape, BlockFace.fromDirection(direction)))
                        continue;

                    placeLight(lightArrays[sectionIdxO], newIndex, newLightLevel);
                    final int sectionXO = (absXO >> 4) & 0x3;
                    final int sectionYO = (absYO >> 4) & 0x3;
                    final int sectionZO = (absZO >> 4) & 0x3;
                    final int sectionPos = (sectionYO << 4) | (sectionZO << 2) | (sectionXO);
                    lightSources.enqueue((sectionPos << 21) | (sectionIdxO << 16) | newIndex | (newLightLevel << 12));
                }
            }
        }
        return lightArrays[sectionIdx3x3(16, 16, 16)];
    }

    /**
     * Computes light in one section
     * <p>
     * Takes queue of lights positions and spreads light from this positions in 3d using Breadth-first search
     *
     * @param blockPalette blocks placed in section
     * @param lightPre     shorts queue in format: [4bit light level][4bit y][4bit z][4bit x]
     * @return lighting wrapped in Result
     */
    public static byte[] compute(Palette blockPalette, ShortArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) return EMPTY_CONTENT;

//        if (true) { // TODO
//            var stone = Palette.empty(64, Palette.BLOCK_PALETTE_MIN_BITS, Palette.BLOCK_PALETTE_MAX_BITS, Palette.BLOCK_PALETTE_DIRECT_BITS);
//            stone.fill(Block.STONE.stateId());
//            var palettes = new Palette[27];
//            Arrays.fill(palettes, stone);
//            palettes[sectionIdx3x3(16, 16, 16)] = blockPalette;
//            var lightPreI = new IntArrayFIFOQueue();
//            final int sectionIdx = sectionIdx3x3(16, 16, 16);
//            while (!lightPre.isEmpty()) {
//                final int index = lightPre.dequeueShort() & 0xFFFF;
//                final int sectionPos = 0b010101;
//                lightPreI.enqueue((sectionPos << 21) | (sectionIdx << 16) | index);
//            }
//            return compute3x3(palettes, lightPreI);
//        }


        final byte[] lightArray = new byte[LIGHT_LENGTH];

        final ShortArrayFIFOQueue lightSources = new ShortArrayFIFOQueue();

        while (!lightPre.isEmpty()) {
            final int index = lightPre.dequeueShort();

            final int newLightLevel = (index >> 12) & 15;
            final int newIndex = index & 0xFFF;

            final int oldLightLevel = getLight(lightArray, newIndex);

            if (oldLightLevel < newLightLevel) {
                placeLight(lightArray, newIndex, newLightLevel);
                lightSources.enqueue((short) index);
            }
        }

        while (!lightSources.isEmpty()) {
            final int index = lightSources.dequeueShort();
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int lightLevel = (index >> 12) & 15;

            for (Direction direction : DIRECTIONS) {
                final int xO = x + direction.normalX();
                final int yO = y + direction.normalY();
                final int zO = z + direction.normalZ();

                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    continue;
                }

                // Section
                final int newIndex = xO | (zO << 4) | (yO << 8);
                final Block targetBlock = Objects.requireNonNullElse(getBlock(blockPalette, xO, yO, zO), Block.AIR);
                final int opacity = targetBlock.registry().lightBlocked();
                final byte newLightLevel = (byte) Math.max(lightLevel - Math.max(opacity, 1), 0);

                if (getLight(lightArray, newIndex) < newLightLevel) {
                    final Block currentBlock = Objects.requireNonNullElse(getBlock(blockPalette, x, y, z), Block.AIR);
                    final Block propagatedBlock = Objects.requireNonNullElse(getBlock(blockPalette, xO, yO, zO), Block.AIR);

                    final Shape currentShape = currentBlock.registry().occlusionShape();
                    final Shape propagatedShape = propagatedBlock.registry().occlusionShape();

                    final boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentShape.isOccluded(propagatedShape, BlockFace.fromDirection(direction)))
                        continue;

                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.enqueue((short) (newIndex | (newLightLevel << 12)));
                }
            }
        }
        return lightArray;
    }

    private static void placeLight(byte[] light, int index, int value) {
        final int shift = (index & 1) << 2;
        final int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    public static int getLight(byte[] light, int x, int y, int z) {
        return getLight(light, x | (z << 4) | (y << 8));
    }

    static int sectionIdx3x3(int x, int y, int z) {
        var sx = x >> 4;
        var sy = y >> 4;
        var sz = z >> 4;
        return sy + sx * 3 + sz * 9;
    }

    static int index3x3(int x, int y, int z) {
        return (x & 0xF) | ((z & 0xF) << 4) | ((y & 0xF) << 8);
    }

    public static int getLight(byte[] light, int index) {
        if (index >>> 1 >= light.length) return 0;
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    public static Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId(palette.get(x, y, z));
    }

    public static byte[] bake(byte @Nullable [] content1, byte @Nullable [] content2) {
        if (content1 == null && content2 == null) return EMPTY_CONTENT;
        if (content1 == EMPTY_CONTENT && content2 == EMPTY_CONTENT) return EMPTY_CONTENT;

        if (content1 == CONTENT_FULLY_LIT) return CONTENT_FULLY_LIT;
        if (content2 == CONTENT_FULLY_LIT) return CONTENT_FULLY_LIT;

        if (content1 == null) return content2;
        if (content2 == null) return content1;

        if (content1 == content2) return content1;

        if (Arrays.equals(content1, EMPTY_CONTENT) && Arrays.equals(content2, EMPTY_CONTENT)) return EMPTY_CONTENT;

        if (Arrays.equals(content1, CONTENT_FULLY_LIT)) return CONTENT_FULLY_LIT;
        if (Arrays.equals(content2, CONTENT_FULLY_LIT)) return CONTENT_FULLY_LIT;

        byte[] lightMax = new byte[LIGHT_LENGTH];
        for (int i = 0; i < LIGHT_LENGTH; i++) {
            final byte c1 = content1[i];
            final byte c2 = content2[i];

            // Lower
            final byte l1 = (byte) (c1 & 0x0F);
            final byte l2 = (byte) (c2 & 0x0F);

            // Upper
            final byte u1 = (byte) ((c1 >> 4) & 0x0F);
            final byte u2 = (byte) ((c2 >> 4) & 0x0F);

            final byte lower = (byte) Math.max(l1, l2);
            final byte upper = (byte) Math.max(u1, u2);

            lightMax[i] = (byte) (lower | (upper << 4));
        }
        return lightMax;
    }

    public static boolean hasBorderChanged(byte[] oldLight, byte[] newLight, BlockFace face) {
        final int k = switch (face) {
            case WEST, BOTTOM, NORTH -> 0;
            case EAST, TOP, SOUTH -> 15;
        };
        for (int bx = 0; bx < SECTION_SIZE; bx++) {
            for (int by = 0; by < SECTION_SIZE; by++) {
                final int posFrom = switch (face) {
                    case NORTH, SOUTH -> bx | (k << 4) | (by << 8);
                    case WEST, EAST -> k | (by << 4) | (bx << 8);
                    default -> bx | (by << 4) | (k << 8);
                };

                final int valueFrom = getLight(oldLight, posFrom);
                final int valueTo = getLight(newLight, posFrom);
                if (valueFrom != valueTo) return true;
            }
        }
        return false;
    }
}
