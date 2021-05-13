package net.minestom.server.raw_data;

public final class RawEntityTypeData {
    public final boolean fireImmune;

    public RawEntityTypeData(boolean fireImmune) {
        this.fireImmune = fireImmune;
    }

    public boolean isFireImmune() {
        return fireImmune;
    }
}