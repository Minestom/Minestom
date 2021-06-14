package net.minestom.server.instance.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectSortedMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

class BlockRegistry {

    // Property name -> values
    private static final Map<String, List<String>> PROPERTIES_MAP = new ConcurrentHashMap<>();
    // Unique name -> IG key
    private static final Map<String, String> PROPERTIES_NAME_MAP = new ConcurrentHashMap<>();

    // Block namespace -> registry data
    private static final Map<String, Block> NAMESPACE_MAP = new ConcurrentHashMap<>();
    // Block id -> registry data
    private static final Map<Integer, Block> BLOCK_ID_MAP = new ConcurrentHashMap<>();
    // Block state -> block object
    private static final Map<Integer, Block> BLOCK_STATE_MAP = new ConcurrentHashMap<>();
    // Block namespace -> properties map to block access
    private static final Map<String, PropertyEntry> BLOCK_PROPERTY_MAP = new ConcurrentHashMap<>();

    private static final Map<NamespaceID, Block> namespaceMap = new HashMap<>();
    private static final Int2ObjectSortedMap<Block> blockSet = new Int2ObjectAVLTreeMap<>();
    private static final Short2ObjectSortedMap<Block.Supplier> stateSet = new Short2ObjectAVLTreeMap<>();

    private static class PropertyEntry {
        private final Map<Map<String, String>, Block> propertyMap = new ConcurrentHashMap<>();
    }

    static {
        // Load data from file

        // Block properties
        JsonObject properties = Registry.load(Registry.Resource.BLOCK_PROPERTY);
        properties.entrySet().forEach(entry -> {
            final String propertyName = entry.getKey();
            final JsonObject propertyObject = entry.getValue().getAsJsonObject();

            final String key = propertyObject.get("key").getAsString();
            final JsonArray values = propertyObject.getAsJsonArray("values");

            List<String> stringValues = new ArrayList<>(values.size());
            values.forEach(jsonElement -> stringValues.add(jsonElement.toString()));

            PROPERTIES_MAP.put(key, stringValues);
            PROPERTIES_NAME_MAP.put(propertyName, key);
        });

        // Blocks
        JsonObject blocks = Registry.load(Registry.Resource.BLOCK);
        blocks.entrySet().forEach(entry -> {
            final String blockNamespace = entry.getKey();
            final JsonObject blockObject = entry.getValue().getAsJsonObject();

            retrieveState(blockNamespace, blockObject);
            final int defaultState = blockObject.get("defaultStateId").getAsInt();
            final Block defaultBlock = getState(defaultState);
            final int id = blockObject.get("id").getAsInt();
            BLOCK_ID_MAP.put(id, defaultBlock);
            NAMESPACE_MAP.put(blockNamespace, defaultBlock);
        });
    }

    private static void retrieveState(String namespace, JsonObject object) {
        final JsonObject states = object.getAsJsonObject("states");

        PropertyEntry propertyEntry = new PropertyEntry();
        states.entrySet().forEach(stateEntry -> {
            final String query = stateEntry.getKey();

            JsonObject stateObject = object.deepCopy();

            stateObject.remove("states");
            stateObject.remove("properties");

            JsonObject stateOverride = stateEntry.getValue().getAsJsonObject();
            stateOverride.entrySet().forEach(entry -> stateObject.add(entry.getKey(), entry.getValue()));
            final int stateId = stateOverride.get("stateId").getAsInt();

            final var propertyMap = getPropertyMap(query);
            final Block block = new BlockTest(stateObject);
            BLOCK_STATE_MAP.put(stateId, block);
            propertyEntry.propertyMap.put(propertyMap, block);
        });
        BLOCK_PROPERTY_MAP.put(namespace, propertyEntry);
    }

    private static Map<String, String> getPropertyMap(String query) {
        Map<String, String> result = new HashMap<>();
        final String propertiesString = query.substring(1, query.length() - 1);
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        StringBuilder builder = keyBuilder;
        for (int i = 0; i < propertiesString.length(); i++) {
            final char c = propertiesString.charAt(i);
            if (c == '=') {
                // Switch to value builder
                builder = valueBuilder;
            } else if (c == ',') {
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

    public static synchronized @Nullable Block get(@NotNull String namespace) {
        return NAMESPACE_MAP.get(namespace);
    }

    public static @Nullable Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    public static @Nullable Block getProperties(String namespace, Map<String, String> properties) {
        final var entry = BLOCK_PROPERTY_MAP.get(namespace);
        return entry.propertyMap.get(properties);
    }

    public static synchronized @Nullable Block fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return namespaceMap.get(namespaceID);
    }

    public static synchronized @Nullable Block fromStateId(short stateId) {
        Block.Supplier supplier = stateSet.get(stateId);
        if (supplier == null) {
            return null;
        }
        return supplier.get(stateId);
    }

    public static synchronized @Nullable Block fromBlockId(int blockId) {
        return blockSet.get(blockId);
    }

    public static synchronized void register(@NotNull NamespaceID namespaceID, @NotNull Block block,
                                             @NotNull IntRange range, @NotNull Block.Supplier blockSupplier) {
        namespaceMap.put(namespaceID, block);
        IntStream.range(range.getMinimum(), range.getMaximum() + 1).forEach(value -> stateSet.put((short) value, blockSupplier));
        blockSet.put(block.getId(), block);
    }
}
