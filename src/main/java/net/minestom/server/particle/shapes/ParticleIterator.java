package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleIterator<T extends ParticleShape> {
    protected final T shape;
    protected final ShapeOptions options;

    protected ParticleIterator(T shape, ShapeOptions options) {
        this.shape = shape;
        this.options = options;
    }

    public void draw(@NotNull Instance instance, @NotNull Position start) {
        draw(instance, start, options.getPatternIterator());
    }

    public abstract void draw(@NotNull Instance instance, @NotNull Position start, @NotNull LinePattern.Iterator pattern);
}
