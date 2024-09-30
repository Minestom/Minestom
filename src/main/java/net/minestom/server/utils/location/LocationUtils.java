package net.minestom.server.utils.location;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class LocationUtils {
    private static final int HORIZONTAL_BIT_MASK = 0x3FFFFFF;
    private static final int VERTICAL_BIT_MASK = 0xFFF;

    private LocationUtils() {}

    /**
     * Gets the block index of a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static long getGlobalBlockIndex(int x, int y, int z) {
        return (((long) x & HORIZONTAL_BIT_MASK) << 38) |
                (((long) z & HORIZONTAL_BIT_MASK) << 12) |
                ((long) y & VERTICAL_BIT_MASK);
    }

    /**
     * @param index  an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the instance position of the block located in {@code index}
     */
    public static @NotNull BlockVec getBlockPosition(long index) {
        final int x = blockIndexToPositionX(index);
        final int y = blockIndexToPositionY(index);
        final int z = blockIndexToPositionZ(index);
        return new BlockVec(x, y, z);
    }

    /**
     * Converts a block index to a position X.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position X of the index
     */
    public static int blockIndexToPositionX(long index) {
        return (int) (index >> 38); // 38-64 bits
    }

    /**
     * Converts a block index to a position Y.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Y of the index
     */
    public static int blockIndexToPositionY(long index) {
        return (int) (index << 52 >> 52); // 0-12 bits
    }

    /**
     * Converts a block index to a position Z.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Z of the index
     */
    public static int blockIndexToPositionZ(long index) {
        return (int) (index << 26 >> 38); // 12-38 bits
    }

    public static void verifyPositionInIndexBounds(int x, int y, int z) {
        Check.argCondition(((x & HORIZONTAL_BIT_MASK) << 6 >> 6) != x, "x position may not be more than 26 bits long");
        Check.argCondition(((y & VERTICAL_BIT_MASK) << 20 >> 20) != y, "y position may not be more than 12 bits long");
        Check.argCondition(((z & HORIZONTAL_BIT_MASK) << 6 >> 6) != z, "z position may not be more than 26 bits long");
    }
}
