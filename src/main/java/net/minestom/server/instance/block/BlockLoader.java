package net.minestom.server.instance.block;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@link Block blocks} from file.
 */
@ApiStatus.Internal
final class BlockLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, Block> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<Block> BLOCK_ID_MAP = new Int2ObjectOpenHashMap<>();
    // Block state -> block object
    private static final Int2ObjectMap<Block> BLOCK_STATE_MAP = new Int2ObjectOpenHashMap<>();

    static Block get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static Block getId(int id) {
        return BLOCK_ID_MAP.get(id);
    }

    static Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    static Collection<Block> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject blocks = Registry.load(Registry.Resource.BLOCKS);
        blocks.entrySet().forEach(entry -> {
            final String blockNamespace = entry.getKey();
            final JsonObject blockObject = entry.getValue().getAsJsonObject();
            final JsonObject stateObject = blockObject.remove("states").getAsJsonObject();

            retrieveState(blockNamespace, blockObject, stateObject);
            final int defaultState = blockObject.get("defaultStateId").getAsInt();
            final Block defaultBlock = getState(defaultState);
            BLOCK_ID_MAP.put(defaultBlock.id(), defaultBlock);
            NAMESPACE_MAP.put(blockNamespace, defaultBlock);
        });
    }

    private static void retrieveState(String namespace, JsonObject object, JsonObject stateObject) {
        PropertyEntry propertyEntry = new PropertyEntry();
        stateObject.entrySet().forEach(stateEntry -> {
            final String query = stateEntry.getKey();
            JsonObject stateOverride = stateEntry.getValue().getAsJsonObject();
            final int stateId = stateOverride.get("stateId").getAsInt();
            final var propertyMap = BlockUtils.parseProperties(query);
            final Block block = new BlockImpl(Registry.block(namespace, object, stateOverride),
                    propertyEntry, propertyMap, null, null);
            BLOCK_STATE_MAP.put(stateId, block);
            propertyEntry.map.put(propertyMap, block);
        });
    }

    protected static class PropertyEntry {
        private final Map<Map<String, String>, Block> map = new ConcurrentHashMap<>();

        public @Nullable Block getProperties(Map<String, String> properties) {
            return map.get(properties);
        }
    }
}
