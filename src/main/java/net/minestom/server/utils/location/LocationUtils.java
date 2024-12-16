package net.minestom.server.utils.location;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class LocationUtils {
    private static final int HORIZONTAL_BIT_MASK = 0x3FFFFFF;
    private static final int VERTICAL_BIT_MASK = 0xFFF;

    private LocationUtils() {}

    /**
     * Gets the global block index of a position.
     * This index is the same as the network encoding of a block location (as of 1.21.1).
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static long getGlobalBlockIndex(int x, int y, int z) {
//        Check.argCondition(((x & HORIZONTAL_BIT_MASK) << 6 >> 6) != x,
//                "x position may not be more than 26 bits long (value: {})", x);
//        Check.argCondition(((y & VERTICAL_BIT_MASK) << 20 >> 20) != y,
//                "y position may not be more than 12 bits long (value: {})", y);
//        Check.argCondition(((z & HORIZONTAL_BIT_MASK) << 6 >> 6) != z,
//                "z position may not be more than 26 bits long (value: {})", z);

        return (((long) x & HORIZONTAL_BIT_MASK) << 38) |
                (((long) z & HORIZONTAL_BIT_MASK) << 12) |
                ((long) y & VERTICAL_BIT_MASK);
    }

    /**
     * Gets the global block index of a position.
     * This index is the same as the network encoding of a block location (as of 1.21.1).
     *
     * @param point The point to turn into an index
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    public static long getGlobalBlockIndex(Point point) {
        return getGlobalBlockIndex(point.blockX(), point.blockY(), point.blockZ());
    }

    /**
     * @param index  an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the instance position of the block located in {@code index}
     */
    public static @NotNull BlockVec getGlobalBlockPosition(long index) {
        final int x = globalBlockIndexToPositionX(index);
        final int y = globalBlockIndexToPositionY(index);
        final int z = globalBlockIndexToPositionZ(index);
        return new BlockVec(x, y, z);
    }

    /**
     * Converts a block index to a position X.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position X of the index
     */
    public static int globalBlockIndexToPositionX(long index) {
        return (int) (index >> 38); // 38-64 bits
    }

    /**
     * Converts a block index to a position Y.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Y of the index
     */
    public static int globalBlockIndexToPositionY(long index) {
        return (int) (index << 52 >> 52); // 0-12 bits
    }

    /**
     * Converts a block index to a position Z.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Z of the index
     */
    public static int globalBlockIndexToPositionZ(long index) {
        return (int) (index << 26 >> 38); // 12-38 bits
    }
}
