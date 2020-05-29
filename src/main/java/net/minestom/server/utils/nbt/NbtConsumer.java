package net.minestom.server.utils.nbt;

@FunctionalInterface
public interface NbtConsumer {
    void accept(NbtWriter writer);
}
