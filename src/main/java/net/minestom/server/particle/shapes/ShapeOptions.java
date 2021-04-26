package net.minestom.server.particle.shapes;

import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO add distance mode (like stretch, continue on other lines, etc.)
public class ShapeOptions {
    private final Particle particle;
    private final ParticleData particleData;
    private final LinePattern linePattern;
    private final int particleDistance;
    private final int particleCount;
    private final float particleSpeed;

    public ShapeOptions(@NotNull Particle particle, @Nullable ParticleData particleData,
                        @NotNull LinePattern linePattern, int particleDistance,
                        int particleCount, float particleSpeed) {
        this.particle = particle;
        this.particleData = particleData;
        this.particleCount = particleCount;
        this.linePattern = linePattern;
        this.particleDistance = particleDistance;
        this.particleSpeed = particleSpeed;
    }

    public @NotNull Particle getParticle() {
        return particle;
    }

    public @Nullable ParticleData getParticleData() {
        return particleData;
    }

    public @NotNull LinePattern.Iterator getPatternIterator() {
        return linePattern.iterator();
    }

    public boolean hasParticleCount() {
        return particleCount != -1;
    }

    public int getParticleDistance() {
        return particleDistance;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public float getParticleSpeed() {
        return particleSpeed;
    }

    public static @NotNull Builder builder(@NotNull Particle particle) {
        return new Builder(particle);
    }

    public static class Builder {
        private Particle particle;
        private ParticleData particleData;
        private LinePattern linePattern = LinePattern.empty();
        private int particleDistance = -1;
        private int particleCount = -1;
        private float particleSpeed = 0;

        private Builder(@NotNull Particle particle) {
            this.particle = particle;
        }

        public @NotNull Builder particle(@NotNull Particle particle) {
            this.particle = particle;
            return this;
        }

        public @NotNull Builder particleData(@Nullable ParticleData particleData) {
            this.particleData = particleData;
            return this;
        }

        public @NotNull Builder linePattern(@NotNull LinePattern linePattern) {
            this.linePattern = linePattern;
            return this;
        }

        public @NotNull Builder particleDistance(int particleDistance) {
            Check.stateCondition(particleCount != -1,
                    "Cannot use particleCount and particleDistance at the same time");
            this.particleDistance = particleDistance;
            return this;
        }

        public @NotNull Builder particleCount(int particleCount) {
            Check.stateCondition(particleDistance != -1,
                    "Cannot use particleCount and particleDistance at the same time");
            this.particleCount = particleCount;
            return this;
        }

        public @NotNull Builder particleSpeed(float particleSpeed) {
            this.particleSpeed = particleSpeed;
            return this;
        }

        public @NotNull ShapeOptions build() {
            if (particleCount == -1 && particleDistance == -1) {
                particleDistance = 1;
            }

            return new ShapeOptions(particle, particleData, linePattern, particleDistance, particleCount, particleSpeed);
        }
    }
}
