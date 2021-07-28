package net.minestom.server.statistic;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
final class StatisticTypeLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, StatisticType> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<StatisticType> ID_MAP = new Int2ObjectOpenHashMap<>();

    static StatisticType get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static StatisticType getId(int id) {
        return ID_MAP.get(id);
    }

    static Collection<StatisticType> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject statistics = Registry.load(Registry.Resource.STATISTICS);
        statistics.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();

            final var statisticType = new StatisticTypeImpl(NamespaceID.from(namespace), id);
            ID_MAP.put(id, statisticType);
            NAMESPACE_MAP.put(namespace, statisticType);
        });
    }
}
