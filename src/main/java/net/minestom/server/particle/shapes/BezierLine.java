package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BezierLine extends ParticleShape {
    private static final double EPSILON = 0.00001;

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
            positions.add(BezierCurves.bezier(points, time));

            time += step;
        }

        this.positions = positions.toArray(Position[]::new);
    }

    @Override
    public BezierIterator iterator(ShapeOptions options) {
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
        public void draw(@NotNull Instance instance, @NotNull Position start) {
            while (hasNext()) {
                Position position = next();
                ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.SOUL_FIRE_FLAME,
                        start.getX() + position.getX(), start.getY() + position.getY(), start.getZ() + position.getZ(),
                        0, 0, 0, 1);

                instance.getPlayers().forEach((player) ->
                        player.getPlayerConnection().sendPacket(packet));
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
