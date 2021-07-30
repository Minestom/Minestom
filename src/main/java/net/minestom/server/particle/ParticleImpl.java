package net.minestom.server.particle;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class ParticleImpl implements Particle {
    private static final Registry.Loader<Particle> LOADER = new Registry.Loader<>();

    static Particle get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static Particle getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static Particle getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<Particle> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject particles = Registry.load(Registry.Resource.PARTICLES);
        particles.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();
            LOADER.register(new ParticleImpl(NamespaceID.from(namespace), id));
        });
    }

    private final NamespaceID namespaceID;
    private final int id;

    ParticleImpl(NamespaceID namespaceID, int id) {
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
