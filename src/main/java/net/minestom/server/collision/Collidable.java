package net.minestom.server.collision;

public interface Collidable {
    double minX();
    double minY();
    double minZ();
    double maxX();
    double maxY();
    double maxZ();

    double height();
    double width();
    double depth();
}
