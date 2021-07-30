package net.minestom.server.potion;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PotionTypeImpl implements PotionType {
    private static final Registry.Loader<PotionType> LOADER = new Registry.Loader<>();

    static PotionType get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static PotionType getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static PotionType getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<PotionType> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject potionTypes = Registry.load(Registry.Resource.POTION_TYPES);
        potionTypes.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();
            LOADER.register(new PotionTypeImpl(NamespaceID.from(namespace), id));
        });
    }

    private final NamespaceID namespaceID;
    private final int id;

    PotionTypeImpl(NamespaceID namespaceID, int id) {
        this.namespaceID = namespaceID;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespaceID;
    }

    @Override
    public int id() {
        return id;
    }
}
