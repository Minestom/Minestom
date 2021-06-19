package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

class CoordinateImpl implements Coordinate {
    private final double x, y, z;

    public CoordinateImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull Coordinate with(double x, double y, double z) {
        return new CoordinateImpl(x, y, z);
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }
}
