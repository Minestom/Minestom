package net.minestom.server.config;

import org.jetbrains.annotations.NotNull;

final class BuilderImpl implements Config.Builder {
    private int compressionThreshold = 256;

    @Override
    public @NotNull BuilderImpl compressionThreshold(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        return this;
    }

    @Override
    public @NotNull Config build() {
        return new ConfigV0(0, compressionThreshold);
    }
}
