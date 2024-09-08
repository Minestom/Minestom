package net.minestom.server.instance.painter;

/**
 * @author Articdive
 */
public final class WhiteNoise {

    /**
     * Returns a predicate that returns true if the noise value at the given position is less than the given chance.
     */
    public static Painter.PosPredicate noise(double chance, long seed) {
        return (x, y, z) -> normalize(evaluate3D(x, y, z, seed)) < chance;
    }

    private static final int X_PRIME = 1619;
    private static final int Y_PRIME = 31337;
    private static final int Z_PRIME = 6971;

    private static double evaluate2D(long x, long y, long seed) {
        int n = (int) ((seed) ^ (X_PRIME * (x)));
        n ^= Y_PRIME * y;

        return (n * n * n * 60493) / 2147483648.0;
    }

    private static double evaluate3D(long x, long y, long z, long seed) {
        int n = (int) ((seed) ^ (X_PRIME * (x)));
        n ^= Y_PRIME * y;
        n ^= Z_PRIME * z;

        return (n * n * n * 60493) / 2147483648.0;
    }

    public static double normalize(double value) {
        return (value + 1.0) * 0.5;
    }
}
