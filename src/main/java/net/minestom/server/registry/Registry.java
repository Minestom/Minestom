package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;

@ApiStatus.Internal
public class Registry {

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static BlockEntry block(@NotNull JsonObject jsonObject) {
        return new BlockEntry(jsonObject);
    }

    public static JsonObject load(Resource resource) {
        final String path = String.format("/%s.json", resource.name);
        final var resourceStream = Registry.class.getResourceAsStream(path);
        return GSON.fromJson(new InputStreamReader(resourceStream), JsonObject.class);
    }

    public enum Resource {
        BLOCK("blocks"),
        BLOCK_PROPERTY("block_properties");

        private final String name;

        Resource(String name) {
            this.name = name;
        }
    }

    public static class BlockEntry extends Entry {
        private BlockEntry(JsonObject json) {
            super(json);
        }

        public String namespace() {
            return getString("namespace");
        }

        public int id() {
            return getInt("id");
        }

        public int stateId() {
            return getInt("stateId");
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
