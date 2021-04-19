package net.minestom.server.particle.shapes;

import net.minestom.server.utils.Position;

public class ParticlePolygon extends CoordinateHolder {
    private final Position[] points;

    public ParticlePolygon(Position[] points) {
        this.points = points;
    }

    public Position[] getPoints() {
        return points;
    }

    public PolygonIterator iterator(int particleCount) {
        return new PolygonIterator(this, particleCount);
    }
}
