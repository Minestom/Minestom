package net.minestom.server.instance;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class BlockLight {
    static final int LIGHT_LENGTH = 16 * 16 * 16 / 2;
    static final int SIDE_LENGTH = 16 * 16 * Direction.values().length / 2;

    static @NotNull Result compute(Palette blockPalette) {
        int sizeX = 16;
        int sizeY = 16;
        int sizeZ = 16;

        byte[] lightArray = new byte[LIGHT_LENGTH];
        byte[][] borders = new byte[Direction.values().length][];
        Arrays.setAll(borders, i -> new byte[SIDE_LENGTH]);

        blockPalette.getAllPresent((x, y, z, stateId) -> {
            final Block block = Block.fromStateId((short) stateId);
            assert block != null;
            final byte lightEmission = (byte) block.registry().lightEmission();
            placeLight(lightArray, x, y, z, lightEmission);
        });

        while (true) {
            boolean updated = false;
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    for (int y = 0; y < sizeY; y++) {
                        final byte light = (byte) getLight(lightArray, x, y, z);
                        byte newLight = light;
                        for (Direction dir : Direction.values()) {
                            int xO = x + dir.normalX();
                            int yO = y + dir.normalY();
                            int zO = z + dir.normalZ();

                            if (xO < 0 || xO >= sizeX || yO < 0 || yO >= sizeY || zO < 0 || zO >= sizeZ) {
                                // TODO: Place border block
                                var border = borders[dir.ordinal()];
                                continue;
                            }

                            byte neighborLight = (byte) (getLight(lightArray, xO, yO, zO) - 1);
                            neighborLight = (byte) ((float) neighborLight * (1 - getBlockFactor(blockPalette, x, y, z)));

                            newLight = (byte) Math.max(newLight, neighborLight);
                        }
                        if (newLight != light) {
                            updated = true;
                            placeLight(lightArray, x, y, z, newLight);
                        }
                    }
                }
            }
            if (!updated) break;
        }


        return new Result(lightArray, null);
    }

    static final class Result {
        final byte[] light;
        final byte[] border;

        Result(byte[] light, byte[] border) {
            assert light.length == LIGHT_LENGTH : "Only 16x16x16 sections are supported: " + light.length;
            //assert border.length == SIDE_LENGTH : "There should be 16x16 entries per side: " + border.length;
            this.light = light;
            this.border = border;
        }

        public byte[] light() {
            return light;
        }

        public Border border(Direction direction) {
            final int length = border.length / Direction.values().length;
            final int index = direction.ordinal() * length;
            return new Border(Arrays.copyOfRange(border, index, index + length));
        }
    }

    record Border(byte[] light) {
        byte get(int x, int z) {
            return light[x + z * 16];
        }
    }

    static void placeLight(byte[] light, int x, int y, int z, int value) {
        int index = (x & 15) | ((z & 15) << 4) | ((y & 15) << 8);
        int shift = (index & 1) << 2;
        int i = index >>> 1;
        light[i] = (byte) ((light[i] & (0xF0 >>> shift)) | (value << shift));
    }

    static int getLight(byte[] light, int x, int y, int z) {
        int index = (x & 15) | ((z & 15) << 4) | ((y & 15) << 8);
        int value = light[index >>> 1];
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    private static float getBlockFactor(Palette palette, int x, int y, int z) {
        var block = Block.fromStateId((short) palette.get(x, y, z));
        assert block != null : "Block not found: " + x + ", " + y + ", " + z;
        var shape = block.registry().collisionShape();
        float factor = shape.relativeStart().isZero() && shape.relativeEnd().samePoint(Vec.ONE) ? 1 : 0;
        return factor;
    }
}
