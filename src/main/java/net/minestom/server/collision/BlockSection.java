package net.minestom.server.collision;

public record BlockSection(double minX, double minY, double minZ, double width, double height, double depth) implements Collidable {
    @Override
    public double maxX() {
        return minX + width;
    }

    @Override
    public double maxY() {
        return minY + height;
    }

    @Override
    public double maxZ() {
        return minZ + depth;
    }
}
