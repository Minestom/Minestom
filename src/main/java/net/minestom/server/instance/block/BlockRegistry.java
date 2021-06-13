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
    // Block namespace -> registry data
    private static final Map<String, JsonObject> BLOCK_MAP = new ConcurrentHashMap<>();

    private static final Map<NamespaceID, Block> namespaceMap = new HashMap<>();
    private static final Int2ObjectSortedMap<Block> blockSet = new Int2ObjectAVLTreeMap<>();
    private static final Short2ObjectSortedMap<Block.Supplier> stateSet = new Short2ObjectAVLTreeMap<>();

    static {
        // Load data from file

        // Block properties
        JsonObject properties = Registry.load(Registry.Resource.BLOCK_PROPERTY);
        properties.keySet().forEach(propertyName -> {
            final JsonObject propertyObject = properties.getAsJsonObject(propertyName);
            final String key = propertyObject.get("key").getAsString();
            final JsonArray values = propertyObject.getAsJsonArray("values");

            List<String> stringValues = new ArrayList<>(values.size());
            values.forEach(jsonElement -> stringValues.add(jsonElement.toString()));

            PROPERTIES_MAP.put(key, stringValues);
        });

        // Blocks
        JsonObject blocks = Registry.load(Registry.Resource.BLOCK);
        blocks.keySet().forEach(blockNamespace -> {
            final JsonObject blockObject = properties.getAsJsonObject(blockNamespace);
            BLOCK_MAP.put(blockNamespace, blockObject);
            final JsonObject propertiesObject = blockObject.getAsJsonObject("properties");
            final JsonArray statesObject = blockObject.getAsJsonArray("states");
            {
                // To do not be cloned over and over
                blockObject.remove("properties");
                blockObject.remove("states");
            }
        });
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
