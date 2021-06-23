package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
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
        private final NamespaceID namespace;
        private final int id;
        private final int stateId;
        private final double hardness;
        private final double explosionResistance;
        private final double friction;
        private final double speedFactor;
        private final double jumpFactor;
        private final boolean air;
        private final boolean solid;
        private final boolean liquid;
        private final boolean blockEntity;

        private BlockEntry(JsonObject main, JsonObject override) {
            super(main, override);
            this.namespace = NamespaceID.from(getString("namespaceId"));
            this.id = getInt("id");
            this.stateId = getInt("stateId");
            this.hardness = getDouble("hardness");
            this.explosionResistance = getDouble("explosionResistance");
            this.friction = getDouble("friction");
            this.speedFactor = getDouble("speedFactor");
            this.jumpFactor = getDouble("jumpFactor");
            this.air = getBoolean("air");
            this.solid = getBoolean("solid");
            this.liquid = getBoolean("liquid");
            this.blockEntity = getBoolean("blockEntity");
        }

        public @NotNull NamespaceID namespace() {
            return namespace;
        }

        public int id() {
            return id;
        }

        public int stateId() {
            return stateId;
        }

        public double hardness() {
            return hardness;
        }

        public double explosionResistance() {
            return explosionResistance;
        }

        public double friction() {
            return friction;
        }

        public double speedFactor() {
            return speedFactor;
        }

        public double jumpFactor() {
            return jumpFactor;
        }

        public boolean isAir() {
            return air;
        }

        public boolean isSolid() {
            return solid;
        }

        public boolean isLiquid() {
            return liquid;
        }

        public boolean isBlockEntity() {
            return blockEntity;
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
