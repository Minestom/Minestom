package net.minestom.server.item;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class MaterialImpl implements Material {
    private static final Registry.Loader<Material> LOADER = new Registry.Loader<>();

    static Material get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static Material getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static Material getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<Material> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject materials = Registry.load(Registry.Resource.ITEMS);
        materials.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject materialObject = entry.getValue().getAsJsonObject();
            LOADER.register(new MaterialImpl(Registry.material(namespace, materialObject, null)));
        });
    }

    private final Registry.MaterialEntry registry;

    MaterialImpl(Registry.MaterialEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.MaterialEntry registry() {
        return registry;
    }
}
