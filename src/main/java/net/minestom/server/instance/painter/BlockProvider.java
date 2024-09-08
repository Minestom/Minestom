package net.minestom.server.instance.painter;

import net.minestom.server.instance.block.Block;

public interface BlockProvider {

    Block get(int x, int y, int z);

    static BlockProvider constant(Block block) {
        return new Constant(block);
    }
    record Constant(Block block) implements BlockProvider {
        @Override
        public Block get(int x, int y, int z) {
            return block;
        }
    }
}
