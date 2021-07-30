package net.minestom.server.sound;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class SoundEventImpl implements SoundEvent {
    private static final Registry.Loader<SoundEvent> LOADER = new Registry.Loader<>();

    static SoundEvent get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static SoundEvent getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static SoundEvent getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<SoundEvent> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject sounds = Registry.load(Registry.Resource.SOUNDS);
        sounds.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();
            LOADER.register(new SoundEventImpl(NamespaceID.from(namespace), id));
        });
    }

    private final NamespaceID namespaceID;
    private final int id;

    SoundEventImpl(NamespaceID namespaceID, int id) {
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
