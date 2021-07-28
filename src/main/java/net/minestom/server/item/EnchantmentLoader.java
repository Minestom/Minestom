package net.minestom.server.item;

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
final class EnchantmentLoader {

    // Maps do not need to be thread-safe as they are fully populated
    // in the static initializer, should not be modified during runtime

    // Block namespace -> registry data
    private static final Map<String, Enchantment> NAMESPACE_MAP = new HashMap<>();
    // Block id -> registry data
    private static final Int2ObjectMap<Enchantment> ID_MAP = new Int2ObjectOpenHashMap<>();

    static Enchantment get(@NotNull String namespace) {
        if (namespace.indexOf(':') == -1) {
            // Default to minecraft namespace
            namespace = "minecraft:" + namespace;
        }
        return NAMESPACE_MAP.get(namespace);
    }

    static Enchantment getId(int id) {
        return ID_MAP.get(id);
    }

    static Collection<Enchantment> values() {
        return Collections.unmodifiableCollection(NAMESPACE_MAP.values());
    }

    static {
        // Load data from file
        JsonObject enchantments = Registry.load(Registry.Resource.ENCHANTMENTS);
        enchantments.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject enchantmentObject = entry.getValue().getAsJsonObject();

            final var enchantment = new EnchantmentImpl(Registry.enchantment(namespace, enchantmentObject, null));
            ID_MAP.put(enchantment.id(), enchantment);
            NAMESPACE_MAP.put(namespace, enchantment);
        });
    }
}
