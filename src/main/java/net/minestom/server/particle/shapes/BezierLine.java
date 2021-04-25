package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class BezierLine extends ParticleShape {
    private final Position start;
    private final Position end;
    private final Position[] controlPoints;
    private final double step;

    public BezierLine(@NotNull Position start, @NotNull Position end,
                      @NotNull Position[] controlPoints,
                      double step) {
        this.start = start;
        this.end = end;
        this.controlPoints = controlPoints;
        this.step = step;
    }

    @Override
    public BezierIterator iterator(ShapeOptions options) {
        return new BezierIterator(this, options);
    }

    public static class BezierIterator extends ParticleIterator<BezierLine> implements Iterator<Position> {
        private double time = 0;

        public BezierIterator(BezierLine shape, ShapeOptions options) {
            super(shape, options);
        }

        @Override
        public boolean hasNext() {
            return time < 1;
        }

        @Override
        public Position next() {
            Position position = BezierCurves.bezier(shape.start, shape.end,
                    shape.controlPoints, time);

            time += shape.step;

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
                "start=" + start +
                ", end=" + end +
                ", controlPoints=" + Arrays.toString(controlPoints) +
                ", step=" + step +
                '}';
    }
}
