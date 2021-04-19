package net.minestom.server.particle.shapes;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;

import java.util.Iterator;

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

    public static class PolygonIterator extends ParticleIterator<ParticlePolygon> implements Iterator<Coords2.LineIterator> {
        private int index = 0;

        protected PolygonIterator(ParticlePolygon polygon, int particleCount) {
            super(polygon, particleCount);
        }

        @Override
        public boolean hasNext() {
            return index < shape.getPoints().length;
        }

        @Override
        public Coords2.LineIterator next() {
            Position position1 = shape.getPoints()[index];
            index++;
            Position position2 = shape.getPoints()[hasNext() ? index : 0];

            return new Coords2(position1, position2).line(particleCount);
        }

        public void draw(Player player) {
            while (hasNext()) {
                Coords2.LineIterator line = next();
                line.draw(player);
            }
        }
    }
}
