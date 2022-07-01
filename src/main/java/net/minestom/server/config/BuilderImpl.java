package net.minestom.server.config;

class BuilderImpl implements Config.Builder {
    private int compressionThreshold = 256;

    @Override
    public BuilderImpl compressionThreshold(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        return this;
    }

    @Override
    public Config build() {
        return new ConfigV0(0, compressionThreshold);
    }
}
