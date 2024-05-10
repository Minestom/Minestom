package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static net.minestom.server.instance.light.BlockLight.buildInternalQueue;

public final class LightCompute {
    static final Direction[] DIRECTIONS = Direction.values();
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SECTION_SIZE = 16;

    public static final byte[] emptyContent = new byte[LIGHT_LENGTH];
    public static final byte[] contentFullyLit = new byte[LIGHT_LENGTH];

    static {
        Arrays.fill(contentFullyLit, (byte) -1);
    }

    static @NotNull Result compute(Palette blockPalette) {
        return LightCompute.compute(blockPalette, buildInternalQueue(blockPalette));
    }

    /**
     * Computes light in one section
     * <p>
     * Takes queue of lights positions and spreads light from this positions in 3d using Breadth-first search
     * @param blockPalette blocks placed in section
     * @param lightPre shorts queue in format: [4bit light level][4bit y][4bit z][4bit x]
     * @return lighting wrapped in Result
     */
    static @NotNull Result compute(Palette blockPalette, ShortArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) {
            return new Result(emptyContent);
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
                    final Block currentBlock = Objects.requireNonNullElse(Block.fromStateId((short)blockPalette.get(x, y, z)), Block.AIR);
                    final Block propagatedBlock = Objects.requireNonNullElse(Block.fromStateId((short)blockPalette.get(xO, yO, zO)), Block.AIR);

                    final boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentBlock.registry().collisionShape().isOccluded(propagatedBlock.registry().collisionShape(), BlockFace.fromDirection(direction))) continue;

                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.enqueue((short) (newIndex | (newLightLevel << 12)));
                }
            }
        }
        return new Result(lightArray);
    }

    record Result(byte[] light) {
        Result {
            assert light.length == LIGHT_LENGTH : "Only 16x16x16 sections are supported: " + light.length;
        }

        public byte getLight(int x, int y, int z) {
            return (byte) LightCompute.getLight(light, x | (z << 4) | (y << 8));
        }
    }

    private static void placeLight(byte[] light, int index, int value) {
        final int shift = (index & 1) << 2;
        final int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    static int getLight(byte[] light, int index) {
        if (index >>> 1 >= light.length) return 0;
        final int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }
}