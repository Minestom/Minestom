package net.minestom.server.utils;

public enum Axis {
    X, Y, Z;

    /**
     * Only works on horizontal axes.
     *
     * @return the perpendicular axis
     */
    public Axis getPerpendicular() {
        return switch (this) {
            case X -> Z;
            case Z -> X;
            case Y -> Y;
        };
    }
}