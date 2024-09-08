package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;

public interface BlockProvider {

    Block get(int x, int y, int z);

    static BlockProvider constant(Block block) {
        return (x, y, z) -> block; // TODO: Make this a record, and optimize for constant blocks
    }
}
