package net.minestom.server.instance.painter;

/**
 * The bounds of this White Noise implementation are: [-1, 1].
 *
 * @author Articdive
 */
final class WhiteNoise {

    /**
     * Constant used for calculating hashes along the X axis.
     */
    public static final int X_PRIME = 1619;
    /**
     * Constant used for calculating hashes along the Y axis.
     */
    public static final int Y_PRIME = 31337;
    /**
     * Constant used for calculating hashes along the Z axis.
     */
    public static final int Z_PRIME = 6971;

    public static double evaluate2D(long x, long y, long seed) {
        int n = (int) ((seed) ^ (X_PRIME * (x)));
        n ^= Y_PRIME * y;

        return (n * n * n * 60493) / 2147483648.0;
    }

    public static double evaluate3D(long x, long y, long z, long seed) {
        int n = (int) ((seed) ^ (X_PRIME * (x)));
        n ^= Y_PRIME * y;
        n ^= Z_PRIME * z;

        return (n * n * n * 60493) / 2147483648.0;
    }
}