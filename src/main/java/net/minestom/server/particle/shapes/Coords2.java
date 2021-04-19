package net.minestom.server.particle.shapes;

import net.minestom.server.utils.Position;

public class Coords2 extends CoordinateHolder {
    private final double x1, y1, z1;
    private final double x2, y2, z2;

    public Coords2(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Coords2(Position position1, Position position2) {
        this(position1.getX(), position1.getY(), position1.getZ(), position2.getX(), position2.getY(), position2.getZ());
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getZ1() {
        return z1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public double getZ2() {
        return z2;
    }

    public LineIterator line(int particleCount) {
        return new LineIterator(this, particleCount);
    }
}
