package net.minestom.server.item;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@link net.minestom.server.item.Material materials} from file.
 */
@ApiStatus.Internal
final class MaterialLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, Material> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<Material> MATERIAL_ID_MAP = new Int2ObjectOpenHashMap<>();

    static @Nullable Material get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static Material getId(int id) {
        return MATERIAL_ID_MAP.get(id);
    }

    static Collection<Material> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject materials = Registry.load(Registry.Resource.ITEM);
        materials.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject materialObject = entry.getValue().getAsJsonObject();

            final Material material = new MaterialImpl(Registry.material(namespace, materialObject, null));
            MATERIAL_ID_MAP.put(material.id(), material);
            NAMESPACE_MAP.put(namespace, material);
        });
    }
}
