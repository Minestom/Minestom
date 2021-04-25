package net.minestom.server.particle.shapes;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShapeOptions {
    private final String linePattern;
    private final int particleDistance;

    public ShapeOptions(@Nullable String linePattern, int particleDistance) {
        Check.argCondition(!validateLinePattern(linePattern), "Line pattern is invalid");
        this.linePattern = linePattern;
        this.particleDistance = particleDistance;
    }

    public @Nullable String getLinePattern() {
        return linePattern;
    }

    public int getParticleDistance() {
        return particleDistance;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    private boolean validateLinePattern(@Nullable String pattern) {
        if (pattern == null) {
            return true;
        }

        for (char c : pattern.toCharArray()) {
            if (!Character.isSpaceChar(c) && c != '-') {
                return false;
            }
        }

        return true;
    }

    public static class Builder {
        private String linePattern;
        private int particleDistance = 1;

        private Builder() {
        }

        public @NotNull Builder linePattern(String linePattern) {
            this.linePattern = linePattern;
            return this;
        }

        public @NotNull Builder particleDistance(int particleDistance) {
            this.particleDistance = particleDistance;
            return this;
        }

        public @NotNull ShapeOptions build() {
            return new ShapeOptions(linePattern, particleDistance);
        }
    }
}
