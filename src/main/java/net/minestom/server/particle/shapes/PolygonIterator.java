package net.minestom.server.particle.shapes;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;

import java.util.Iterator;

public class PolygonIterator extends ParticleIterator<ParticlePolygon> implements Iterator<LineIterator> {
    private int index = 0;

    protected PolygonIterator(ParticlePolygon polygon, int particleCount) {
        super(polygon, particleCount);
    }

    @Override
    public boolean hasNext() {
        return index < shape.getPoints().length;
    }

    @Override
    public LineIterator next() {
        Position position1 = shape.getPoints()[index];
        index++;
        Position position2 = shape.getPoints()[hasNext() ? index : 0];

        return new Coords2(position1, position2).line(particleCount);
    }

    public void draw(Player player) {
        while (hasNext()) {
            LineIterator line = next();
            line.draw(player);
        }
    }
}
