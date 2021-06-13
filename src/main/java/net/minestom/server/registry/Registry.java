package net.minestom.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Registry {

    private static final Loader LOADER = new Loader();

    public static BlockEntry block(@NotNull Block block) {
        return loader().block(block.getName());
    }

    @ApiStatus.Internal
    public static @NotNull Loader loader() {
        return LOADER;
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

    public static class Loader {
        private final RegistryMap<BlockEntry> blockRegistry = new RegistryMap<>(BlockEntry::new);

        public void loadBlocks(@NotNull JsonObject blocks) {
            loadRegistry(blockRegistry, blocks);
        }

        public BlockEntry block(String name) {
            return blockRegistry.get(name);
        }

        private <T extends Entry> void loadRegistry(RegistryMap<T> map, JsonObject data) {
            data.keySet().forEach(namespace -> {
                final JsonObject value = data.get(namespace).getAsJsonObject();
                map.put(namespace, map.function.apply(value));
            });
        }
    }

    private static class RegistryMap<T extends Entry> extends ConcurrentHashMap<String, T> {
        private final Function<JsonObject, T> function;

        private RegistryMap(Function<JsonObject, T> function) {
            this.function = function;
        }
    }
}
