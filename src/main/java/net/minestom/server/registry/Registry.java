package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.Objects;

@ApiStatus.Internal
public class Registry {

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static BlockEntry block(@NotNull JsonObject jsonObject, JsonObject override) {
        return new BlockEntry(jsonObject, override);
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
        private BlockEntry(JsonObject main, JsonObject override) {
            super(main, override);
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

        public double destroySpeed() {
            return getDouble("destroySpeed");
        }

        public double explosionResistance() {
            return getDouble("explosionResistance");
        }

        public double friction() {
            return getDouble("friction");
        }

        public double speedFactor() {
            return getDouble("speedFactor");
        }

        public double jumpFactor() {
            return getDouble("jumpFactor");
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
        private final JsonObject main, override;

        private Entry(JsonObject main, JsonObject override) {
            this.main = main;
            this.override = override;
        }

        public String getString(String name) {
            return element(name).getAsString();
        }

        public double getDouble(String name) {
            return element(name).getAsDouble();
        }

        public int getInt(String name) {
            return element(name).getAsInt();
        }

        public boolean getBoolean(String name) {
            return element(name).getAsBoolean();
        }

        protected JsonElement element(String name) {
            return Objects.requireNonNullElseGet(override.get(name), () -> main.get(name));
        }
    }
}
