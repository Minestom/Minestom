package net.minestom.server.instance.light;

import it.unimi.dsi.fastutil.shorts.ShortArrayFIFOQueue;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Objects;

import static net.minestom.server.instance.light.BlockLight.buildInternalQueue;

public final class LightCompute {
    static final BlockFace[] FACES = BlockFace.values();
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SECTION_SIZE = 16;

    public static final byte[] emptyContent = new byte[LIGHT_LENGTH];

    static @NotNull Result compute(Palette blockPalette) {
        return LightCompute.compute(blockPalette, buildInternalQueue(blockPalette));
    }

    static @NotNull Result compute(Palette blockPalette, ShortArrayFIFOQueue lightPre) {
        if (lightPre.isEmpty()) {
            return new Result(emptyContent);
        }

        byte[] lightArray = new byte[LIGHT_LENGTH];

        var lightSources = new ArrayDeque<Short>();

        while (!lightPre.isEmpty()) {
            int index = lightPre.dequeueShort();

            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int newLightLevel = (index >> 12) & 15;
            final int newIndex = x | (z << 4) | (y << 8);

            final int oldLightLevel = getLight(lightArray, newIndex);

            if (oldLightLevel < newLightLevel) {
                placeLight(lightArray, newIndex, newLightLevel);
                lightSources.add((short) index);
            }
        }

        while (!lightSources.isEmpty()) {
            final int index = lightSources.poll();
            final int x = index & 15;
            final int z = (index >> 4) & 15;
            final int y = (index >> 8) & 15;
            final int lightLevel = (index >> 12) & 15;

            for (BlockFace face : FACES) {
                Direction dir = face.toDirection();
                final int xO = x + dir.normalX();
                final int yO = y + dir.normalY();
                final int zO = z + dir.normalZ();
                final byte newLightLevel = (byte) (lightLevel - 1);

                // Handler border
                if (xO < 0 || xO >= SECTION_SIZE || yO < 0 || yO >= SECTION_SIZE || zO < 0 || zO >= SECTION_SIZE) {
                    continue;
                }

                // Section
                final int newIndex = xO | (zO << 4) | (yO << 8);
                if (getLight(lightArray, newIndex) + 2 <= lightLevel) {
                    final Block currentBlock = Objects.requireNonNullElse(Block.fromStateId((short)blockPalette.get(x, y, z)), Block.AIR);
                    final Block propagatedBlock = Objects.requireNonNullElse(Block.fromStateId((short)blockPalette.get(xO, yO, zO)), Block.AIR);

                    boolean airAir = currentBlock.isAir() && propagatedBlock.isAir();
                    if (!airAir && currentBlock.registry().collisionShape().isOccluded(propagatedBlock.registry().collisionShape(), face)) continue;
                    placeLight(lightArray, newIndex, newLightLevel);
                    lightSources.add((short) (newIndex | (newLightLevel << 12)));
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