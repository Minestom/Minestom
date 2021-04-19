package net.minestom.server.particle.shapes;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleIterator<T extends ParticleShape> {
    protected final T shape;
    protected final int particleCount;

    protected ParticleIterator(T shape, int particleCount) {
        this.shape = shape;
        this.particleCount = particleCount;
    }

    public abstract void draw(@NotNull Instance instance);
}
