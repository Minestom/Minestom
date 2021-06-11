package net.minestom.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;

public class Registry {

    public static BlockEntry block(Block block) {
        return new BlockEntry(new JsonObject());
    }

    public static class BlockEntry extends Entry {
        private BlockEntry(JsonObject json) {
            super(json);
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

        private final JsonObject json;

        private Entry(JsonObject json) {
            this.json = json;
        }

        public String getString(String name) {
            return element(name).getAsString();
        }

        public float getFloat(String name) {
            return element(name).getAsFloat();
        }

        public int getInt(String name) {
            return element(name).getAsInt();
        }

        public boolean getBoolean(String name) {
            return element(name).getAsBoolean();
        }

        protected JsonElement element(String name) {
            return json.get(name);
        }
    }
}
