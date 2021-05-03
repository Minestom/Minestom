package net.minestom.server.particle;

import net.minestom.server.particle.data.ParticleEffect;
import org.jetbrains.annotations.NotNull;

public class ParticleBuilder {
    private final Particle particle;

    private boolean longDistance = true;
    private float offsetX, offsetY, offsetZ = 0;
    private float speed = 0;
    private int count = 1;

    public ParticleBuilder(Particle particle) {
        this.particle = particle;
    }

    /**
     * Sets whether the client should force this particle to show, even when it is far away
     *
     * @param longDistance if the client should force show this particle
     * @return this
     */
    public @NotNull ParticleBuilder longDistance(boolean longDistance) {
        this.longDistance = longDistance;
        return this;
    }

    /**
     * Sets the random offset of this particle
     *
     * @param offsetX random x offset
     * @param offsetY random y offset
     * @param offsetZ random z offset
     * @return this
     */
    public @NotNull ParticleBuilder offset(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        return this;
    }

    /**
     * Sets the speed of this particle
     *
     * @param speed speed of this particle
     * @return this
     */
    public @NotNull ParticleBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the amount of times this particle is shown.
     *
     * @param count the amount of times this particle is shown
     * @return this
     */
    public @NotNull ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    /**
     * Creates a {@link ParticleEffect} without data.
     *
     * @param particle the type of the particle
     * @return the {@link ParticleEffect} formed from the particle type
     */
    public @NotNull Particle build(@NotNull Particle particle) {
        return particle.with(longDistance, offsetX, offsetY, offsetZ, speed, count);
    }
}
