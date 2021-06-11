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

        public float destroySpeed() {
            return getFloat("destroySpeed");
        }

        public float explosionResistance() {
            return getFloat("explosionResistance");
        }

        public float friction() {
            return getFloat("friction");
        }

        public float speedFactor() {
            return getFloat("speedFactor");
        }

        public float jumpFactor() {
            return getFloat("jumpFactor");
        }

        public boolean isAir() {
            return getBoolean("air");
        }

        public boolean isSolid() {
            return getBoolean("solid");
        }

        public boolean isLiquid() {
            return getBoolean("liquid");
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

        public float getFloat(String path) {
            return 0;
        }

        public int getInt(String path) {
            return 0;
        }

        public boolean getBoolean(String path) {
            return false;
        }
    }
}
