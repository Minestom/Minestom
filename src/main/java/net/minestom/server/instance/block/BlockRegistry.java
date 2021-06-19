package net.minestom.server.instance.block;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class BlockRegistry {

    // Block namespace -> registry data
    private static final Map<String, Block> NAMESPACE_MAP = new ConcurrentHashMap<>();
    // Block id -> registry data
    private static final Map<Integer, Block> BLOCK_ID_MAP = new ConcurrentHashMap<>();
    // Block state -> block object
    private static final Map<Integer, Block> BLOCK_STATE_MAP = new ConcurrentHashMap<>();
    // Block namespace -> properties map to block access
    private static final Map<String, PropertyEntry> BLOCK_PROPERTY_MAP = new ConcurrentHashMap<>();

    static @Nullable Block get(@NotNull String namespace) {
        return NAMESPACE_MAP.get(namespace);
    }

    static @Nullable Block getId(int id) {
        return BLOCK_ID_MAP.get(id);
    }

    static @Nullable Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    static @Nullable Block getProperties(String namespace, Map<String, String> properties) {
        final var entry = BLOCK_PROPERTY_MAP.get(namespace);
        return entry.propertyMap.get(properties);
    }

    static @Nullable Block getProperties(Block block, Map<String, String> properties) {
        return getProperties(block.getNamespaceId().asString(), properties);
    }

    static {
        // Load data from file
        JsonObject blocks = Registry.load(Registry.Resource.BLOCK);
        blocks.entrySet().forEach(entry -> {
            final String blockNamespace = entry.getKey();
            final JsonObject blockObject = entry.getValue().getAsJsonObject();
            final JsonObject stateObject = blockObject.remove("states").getAsJsonObject();
            blockObject.remove("properties");

            blockObject.addProperty("namespace", blockNamespace);

            retrieveState(blockNamespace, blockObject, stateObject);
            final int defaultState = blockObject.get("defaultStateId").getAsInt();
            final Block defaultBlock = getState(defaultState);
            final int id = blockObject.get("id").getAsInt();
            BLOCK_ID_MAP.put(id, defaultBlock);
            NAMESPACE_MAP.put(blockNamespace, defaultBlock);
        });
    }

    private static class PropertyEntry {
        private final Map<Map<String, String>, Block> propertyMap = new ConcurrentHashMap<>();
    }

    private static void retrieveState(String namespace, JsonObject object, JsonObject stateObject) {
        PropertyEntry propertyEntry = new PropertyEntry();
        stateObject.entrySet().forEach(stateEntry -> {
            final String query = stateEntry.getKey();
            JsonObject stateOverride = stateEntry.getValue().getAsJsonObject();
            final int stateId = stateOverride.get("stateId").getAsInt();
            final var propertyMap = getPropertyMap(query);
            final Block block = new BlockTest(Registry.block(object, stateOverride), propertyMap);
            BLOCK_STATE_MAP.put(stateId, block);
            propertyEntry.propertyMap.put(propertyMap, block);
        });
        BLOCK_PROPERTY_MAP.put(namespace, propertyEntry);
    }

    private static Map<String, String> getPropertyMap(String query) {
        if (query.equals("[]")) {
            return Collections.emptyMap();
        }
        final int capacity = StringUtils.countMatches(query, ',') + 1;
        Map<String, String> result = new HashMap<>(capacity);
        final String propertiesString = query.substring(1);
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        StringBuilder builder = keyBuilder;
        for (int i = 0; i < propertiesString.length(); i++) {
            final char c = propertiesString.charAt(i);
            if (c == '=') {
                // Switch to value builder
                builder = valueBuilder;
            } else if (c == ',' || c == ']') {
                // Append current text
                result.put(keyBuilder.toString(), valueBuilder.toString());
                keyBuilder = new StringBuilder();
                valueBuilder = new StringBuilder();
                builder = keyBuilder;
            } else if (c != ' ') {
                builder.append(c);
            }
        }
        return result;
    }
}
