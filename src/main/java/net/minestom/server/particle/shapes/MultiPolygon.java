package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

//TODO better name
public class MultiPolygon extends ParticleShape {
    private final ParticleShape[] shapes;

    public MultiPolygon(@NotNull ParticleShape[] shapes) {
        this.shapes = shapes;
    }

    @Override
    public ParticleIterator<?> iterator(int particleCount) {
        return new MultiPolygonIterator(this, particleCount);
    }

    public static class MultiPolygonIterator extends ParticleIterator<MultiPolygon> implements Iterator<ParticleShape> {
        private int index = 0;

        protected MultiPolygonIterator(MultiPolygon shape, int particleCount) {
            super(shape, particleCount);
        }

        @Override
        public boolean hasNext() {
            return index < shape.shapes.length;
        }

        @Override
        public ParticleShape next() {
            ParticleShape result = shape.shapes[index];

            index++;

            return result;
        }

        @Override
        public void draw(@NotNull Instance instance, @NotNull Position start) {
            while (hasNext()) {
                ParticleShape shape = next();
                shape.iterator(particleCount).draw(instance, start);
            }
        }
    }
}
