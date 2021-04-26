package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.ParticleUtils;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ParticleSingle extends ParticleShape {
    private final double x, y, z;

    public ParticleSingle(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ParticleSingle(@NotNull Position position) {
        this(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public @NotNull SingleIterator iterator(ShapeOptions options) {
        return new SingleIterator(this, options);
    }

    public static class SingleIterator extends ParticleIterator<ParticleSingle> implements Iterator<Position> {
        private boolean done = false;

        protected SingleIterator(ParticleSingle shape, ShapeOptions options) {
            super(shape, options);
        }

        @Override
        public boolean hasNext() {
            return !done;
        }

        @Override
        public Position next() {
            done = true;

            return new Position(shape.x, shape.y, shape.z);
        }

        @Override
        public void draw(@NotNull Instance instance, @NotNull Position start, @NotNull LinePattern.Iterator pattern) {
            Position position = next();
            if (pattern.next()) {
                ParticleUtils.drawParticle(instance, start.clone().add(position),
                        options.getParticle(), options.getParticleData());
            }
        }
    }
}
