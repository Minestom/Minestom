package net.minestom.server.potion;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
final class PotionEffectLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, PotionEffect> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<PotionEffect> ID_MAP = new Int2ObjectOpenHashMap<>();

    static PotionEffect get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static PotionEffect getId(int id) {
        return ID_MAP.get(id);
    }

    static Collection<PotionEffect> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject potionEffects = Registry.load(Registry.Resource.POTION_EFFECTS);
        potionEffects.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();

            final var potionEffect = new PotionEffectImpl(Registry.potionEffect(namespace, object, null));
            ID_MAP.put(potionEffect.id(), potionEffect);
            NAMESPACE_MAP.put(namespace, potionEffect);
        });
    }
}
