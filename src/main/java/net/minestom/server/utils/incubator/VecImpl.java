package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

class VecImpl implements Vec {
    private final double x, y, z;

    public VecImpl(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VecImpl(double value) {
        this(value, value, value);
    }

    @Override
    public @NotNull Vec with(double x, double y, double z) {
        return new VecImpl(x, y, z);
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
