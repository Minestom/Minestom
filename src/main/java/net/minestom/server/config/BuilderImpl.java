package net.minestom.server.config;

class BuilderImpl implements Config.Builder {

    @Override
    public Config build() {
        return new ConfigV0(0);
    }
}
