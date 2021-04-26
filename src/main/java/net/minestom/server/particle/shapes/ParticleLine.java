package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.ParticleUtils;
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

    public @NotNull LineIterator iterator(ShapeOptions options) {
        return new LineIterator(this, options);
    }

    public static class LineIterator extends ParticleIterator<ParticleLine> implements Iterator<Position> {
        private final double changeX, changeY, changeZ;
        private final int particleCount;

        private double x, y, z;
        private int particles = 0;

        public LineIterator(@NotNull ParticleLine shape, ShapeOptions options) {
            super(shape, options);

            if (options.hasParticleCount()) {
                this.particleCount = options.getParticleCount();
            } else {
                //Stretch behavior
                this.particleCount = (int) Math.round(shape.length / options.getParticleDistance());
            }

            this.changeX = shape.dx / particleCount;
            this.changeY = shape.dy / particleCount;
            this.changeZ = shape.dz / particleCount;

            this.x = shape.x1;
            this.y = shape.y1;
            this.z = shape.z1;
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
        public void draw(@NotNull Instance instance, @NotNull Position start, @NotNull LinePattern.Iterator pattern) {
            while (hasNext()) {
                Position position = next();
                if (pattern.next()) {
                    ParticleUtils.drawParticle(instance, start.clone().add(position),
                            options.getParticle(), options.getParticleData());
                }
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
