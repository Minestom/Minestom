package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ParticleCircle extends ParticleShape {
    private final double x, y, z;
    private final double radius;
    private final int particleCount;

    public ParticleCircle(double x, double y, double z, double radius, int particleCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.particleCount = particleCount;
    }

    @Override
    public CircleIterator iterator(ShapeOptions options) {
        return new CircleIterator(this, options);
    }

    public static class CircleIterator extends ParticleIterator<ParticleCircle> implements Iterator<Position> {
        private double currentAngle = 0;

        public CircleIterator(ParticleCircle shape, ShapeOptions options) {
            super(shape, options);
        }

        @Override
        public boolean hasNext() {
            return currentAngle < 0;
        }

        @Override
        public Position next() {
            return null;
        }

        @Override
        public void draw(@NotNull Instance instance, @NotNull Position start) {

        }
    }
}
