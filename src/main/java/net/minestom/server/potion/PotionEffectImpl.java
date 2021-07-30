package net.minestom.server.potion;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PotionEffectImpl implements PotionEffect {
    private static final Registry.Loader<PotionEffect> LOADER = new Registry.Loader<>();

    static PotionEffect get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static PotionEffect getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static PotionEffect getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<PotionEffect> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject potionEffects = Registry.load(Registry.Resource.POTION_EFFECTS);
        potionEffects.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            LOADER.register(new PotionEffectImpl(Registry.potionEffect(namespace, object, null)));
        });
    }

    private final Registry.PotionEffectEntry registry;

    PotionEffectImpl(Registry.PotionEffectEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.PotionEffectEntry registry() {
        return registry;
    }
}
