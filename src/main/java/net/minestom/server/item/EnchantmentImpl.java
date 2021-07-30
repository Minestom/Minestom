package net.minestom.server.item;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class EnchantmentImpl implements Enchantment {
    private static final Registry.Loader<Enchantment> LOADER = new Registry.Loader<>();

    static Enchantment get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static Enchantment getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static Enchantment getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<Enchantment> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject enchantments = Registry.load(Registry.Resource.ENCHANTMENTS);
        enchantments.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject enchantmentObject = entry.getValue().getAsJsonObject();
            LOADER.register(new EnchantmentImpl(Registry.enchantment(namespace, enchantmentObject, null)));
        });
    }

    private final Registry.EnchantmentEntry registry;

    EnchantmentImpl(Registry.EnchantmentEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.EnchantmentEntry registry() {
        return registry;
    }
}
