package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.ParticleUtils;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BezierLine extends ParticleShape {
    private final Position[] positions;

    public BezierLine(@NotNull Position start, @NotNull Position end,
                      @NotNull Position[] controlPoints,
                      double step) {
        List<Position> positions = new ArrayList<>();

        Position[] points = new Position[2 + controlPoints.length];
        points[0] = start;
        System.arraycopy(controlPoints, 0, points, 1, controlPoints.length);
        points[points.length - 1] = end;

        double time = 0;

        while (time <= 1 + EPSILON) {
            positions.add(ParticleUtils.bezier(points, time));

            time += step;
        }

        this.positions = positions.toArray(Position[]::new);
    }

    //TODO options is not fully used for bezier curves (only linePattern)
    @Override
    public @NotNull BezierIterator iterator(ShapeOptions options) {
        return new BezierIterator(this, options);
    }

    public static class BezierIterator extends ParticleIterator<BezierLine> implements Iterator<Position> {
        private int index = 0;

        public BezierIterator(BezierLine shape, ShapeOptions options) {
            super(shape, options);
        }

        @Override
        public boolean hasNext() {
            return index < shape.positions.length;
        }

        @Override
        public Position next() {
            Position position = shape.positions[index];

            index++;

            return position;
        }

        @Override
        public void draw(@NotNull Instance instance, @NotNull Position start, @NotNull LinePattern.Iterator pattern) {
            while (hasNext()) {
                Position position = next();
                if (pattern.next()) {
                    ParticleUtils.drawParticle(instance, start.clone().add(position), options);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "BezierLine{" +
                "positions=" + Arrays.toString(positions) +
                '}';
    }
}
