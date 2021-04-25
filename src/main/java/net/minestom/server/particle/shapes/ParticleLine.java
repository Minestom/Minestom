package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ParticleLine extends ParticleShape {
    private final double x1, y1, z1;
    private final double x2, y2, z2;
    private final double dx, dy, dz;
    private final double length;

    public ParticleLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;

        this.dx = x2 - x1;
        this.dy = y2 - y1;
        this.dz = z2 - z1;

        this.length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public ParticleLine(@NotNull Position position1, @NotNull Position position2) {
        this(position1.getX(), position1.getY(), position1.getZ(),
                position2.getX(), position2.getY(), position2.getZ());
    }

    public LineIterator iterator(ShapeOptions options) {
        return new LineIterator(this, options);
    }

    public static class LineIterator extends ParticleIterator<ParticleLine> implements Iterator<Position> {
        private final double changeX, changeY, changeZ;
        private final int particleCount;

        private double x, y, z;
        private int particles = 0;

        public LineIterator(@NotNull ParticleLine line, ShapeOptions options) {
            super(line, options);

            //Stretch behavior
            this.particleCount = (int) Math.round(line.length / options.getParticleDistance());

            this.changeX = line.dx / particleCount;
            this.changeY = line.dy / particleCount;
            this.changeZ = line.dz / particleCount;

            this.x = line.x1;
            this.y = line.y1;
            this.z = line.z1;
        }

        @Override
        public boolean hasNext() {
            return particles < particleCount;
        }

        @Override
        public Position next() {
            Position position = new Position(x, y, z);

            particles++;

            x += changeX;
            y += changeY;
            z += changeZ;

            return position;
        }

        @Override
        public void draw(@NotNull Instance instance, @NotNull Position start) {
            while (hasNext()) {
                Position position = next();
                ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.FLAME,
                        start.getX() + position.getX(), start.getY() + position.getY(), start.getZ() + position.getZ(),
                        0, 0, 0, 1);

                instance.getPlayers().forEach((player) ->
                        player.getPlayerConnection().sendPacket(packet));
            }
        }
    }

    @Override
    public String toString() {
        return "ParticleLine{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", z1=" + z1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", z2=" + z2 +
                '}';
    }
}
