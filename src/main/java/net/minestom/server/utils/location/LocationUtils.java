package net.minestom.server.utils.location;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class LocationUtils {
    private static final int X_BIT_OFFSET = 38;
    private static final int Z_BIT_OFFSET = 12;
    private static final int Y_BIT_OFFSET = 0;

    private static final int HORIZONTAL_BIT_SIZE = 26;
    private static final int VERTICAL_BIT_SIZE   = 12;

    private static final int HORIZONTAL_BIT_MASK = (1 << HORIZONTAL_BIT_SIZE) - 1;
    private static final int VERTICAL_BIT_MASK =   (1 << VERTICAL_BIT_SIZE)   - 1;

    private static final int X_SIGN_EXTEND_SHIFT_LEFT  = Long.SIZE - (HORIZONTAL_BIT_SIZE + X_BIT_OFFSET);
    private static final int X_SIGN_EXTEND_SHIFT_RIGHT = Long.SIZE - (HORIZONTAL_BIT_SIZE);
    private static final int Z_SIGN_EXTEND_SHIFT_LEFT  = Long.SIZE - (HORIZONTAL_BIT_SIZE + Z_BIT_OFFSET);
    private static final int Z_SIGN_EXTEND_SHIFT_RIGHT = Long.SIZE - (HORIZONTAL_BIT_SIZE);
    private static final int Y_SIGN_EXTEND_SHIFT_LEFT  = Long.SIZE - (VERTICAL_BIT_SIZE + Y_BIT_OFFSET);
    private static final int Y_SIGN_EXTEND_SHIFT_RIGHT = Long.SIZE - (VERTICAL_BIT_SIZE);

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
        return ( ((long) x & HORIZONTAL_BIT_MASK) << X_BIT_OFFSET) |
                (((long) z & HORIZONTAL_BIT_MASK) << Z_BIT_OFFSET) |
                (((long) y & VERTICAL_BIT_MASK)   << Y_BIT_OFFSET);
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
        return (int) (index << X_SIGN_EXTEND_SHIFT_LEFT >> X_SIGN_EXTEND_SHIFT_RIGHT);
    }

    /**
     * Converts a block index to a position Y.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Y of the index
     */
    public static int globalBlockIndexToPositionY(long index) {
        return (int) (index << Y_SIGN_EXTEND_SHIFT_LEFT >> Y_SIGN_EXTEND_SHIFT_RIGHT);
    }

    /**
     * Converts a block index to a position Z.
     *
     * @param index an index computed from {@link #getGlobalBlockIndex(int, int, int)}
     * @return the position Z of the index
     */
    public static int globalBlockIndexToPositionZ(long index) {
        return (int) (index << Z_SIGN_EXTEND_SHIFT_LEFT >> Z_SIGN_EXTEND_SHIFT_RIGHT);
    }
}
