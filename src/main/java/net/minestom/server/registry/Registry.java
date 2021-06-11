package net.minestom.server.registry;

import net.minestom.server.instance.block.Block;

public class Registry {

    public static BlockEntry block(Block block) {
        return new BlockEntry("blocks.json");
    }

    public static class BlockEntry extends Entry {
        private BlockEntry(String resource) {
            super(resource);
        }

        public boolean isSolid() {
            return getBoolean("solid");
        }
    }

    public static class Entry {

        private final String resource;

        private Entry(String resource) {
            this.resource = resource;
        }

        public String getString(String path) {
            return null;
        }

        public int getInt(String path) {
            return 0;
        }

        public boolean getBoolean(String path) {
            return false;
        }
    }
}
