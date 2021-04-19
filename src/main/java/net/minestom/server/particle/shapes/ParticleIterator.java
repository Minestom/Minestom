package net.minestom.server.particle.shapes;

public abstract class ParticleIterator<T extends CoordinateHolder> {
    protected final T shape;
    protected final int particleCount;

    protected ParticleIterator(T shape, int particleCount) {
        this.shape = shape;
        this.particleCount = particleCount;
    }
}
