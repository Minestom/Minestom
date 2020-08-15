package net.minestom.server.instance.block;

@FunctionalInterface
public interface BlockProvider {
    short getBlockStateId(int x, int y, int z);
}
