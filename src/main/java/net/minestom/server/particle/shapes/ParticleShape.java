package net.minestom.server.particle.shapes;

public abstract class ParticleShape {
    public abstract ParticleIterator<?> iterator(int particleCount);
}
