package net.minestom.server.world.generator;

import net.minestom.server.instance.block.Block;

public interface BlockProvider {
    Block getBlock(int x, int y, int z, int heightXZ);
}
