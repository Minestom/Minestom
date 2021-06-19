package net.minestom.server.utils.incubator;

import org.jetbrains.annotations.NotNull;

public class Pos implements Coordinate {

    private final double x, y, z;

    public Pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull Pos with(double x, double y, double z) {
        return new Pos(x, y, z);
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
