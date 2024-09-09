package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.collision.Shape;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public final class LightCompute {
    static final Direction[] DIRECTIONS = Direction.values();
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SECTION_SIZE = 16;

    public static final byte[] EMPTY_CONTENT = new byte[LIGHT_LENGTH];
    public static final byte[] CONTENT_FULLY_LIT = new byte[LIGHT_LENGTH];

    static {
        Arrays.fill(CONTENT_FULLY_LIT, (byte) -1);
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
    static byte @NotNull [] compute(Palette blockPalette, ShortArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) {
            return EMPTY_CONTENT;
        }

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
            final byte newLightLevel = (byte) (lightLevel - 1);

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

                if (getLight(lightArray, newIndex) < newLightLevel) {
                    final Block currentBlock = Objects.requireNonNullElse(getBlock(blockPalette, x, y, z), Block.AIR);
                    final Block propagatedBlock = Objects.requireNonNullElse(getBlock(blockPalette, xO, yO, zO), Block.AIR);

                    final Shape currentShape = currentBlock.registry().collisionShape();
                    final Shape propagatedShape = propagatedBlock.registry().collisionShape();

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

    static int getLight(byte[] light, int x, int y, int z) {
        return getLight(light, x | (z << 4) | (y << 8));
    }

    static int getLight(byte[] light, int index) {
        if (index >>> 1 >= light.length) return 0;
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    public static Block getBlock(Palette palette, int x, int y, int z) {
        return Block.fromStateId((short) palette.get(x, y, z));
    }

    public static byte[] bake(byte[] content1, byte[] content2) {
        if (content1 == null && content2 == null) return EMPTY_CONTENT;
        if (content1 == EMPTY_CONTENT && content2 == EMPTY_CONTENT) return EMPTY_CONTENT;

        if (content1 == null) return content2;
        if (content2 == null) return content1;

        if (Arrays.equals(content1, EMPTY_CONTENT) && Arrays.equals(content2, EMPTY_CONTENT)) return EMPTY_CONTENT;

        byte[] lightMax = new byte[LIGHT_LENGTH];
        for (int i = 0; i < content1.length; i++) {
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

    public static boolean compareBorders(byte[] content, byte[] contentPropagation, byte[] contentPropagationTemp, BlockFace face) {
        if (content == null && contentPropagation == null && contentPropagationTemp == null) return true;

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

                int valueFrom;

                if (content == null && contentPropagation == null) valueFrom = 0;
                else if (content != null && contentPropagation == null) valueFrom = getLight(content, posFrom);
                else if (content == null) valueFrom = getLight(contentPropagation, posFrom);
                else valueFrom = Math.max(getLight(content, posFrom), getLight(contentPropagation, posFrom));

                final int valueTo = getLight(contentPropagationTemp, posFrom);

                if (valueFrom < valueTo) return false;
            }
        }
        return true;
    }
}
