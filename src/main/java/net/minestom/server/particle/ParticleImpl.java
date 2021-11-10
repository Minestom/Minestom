package net.minestom.server.particle;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class ParticleImpl implements Particle {
    private static final Registry.Container<Particle> CONTAINER = new Registry.Container<>(Registry.Resource.PARTICLES,
            (loader, namespace, object) -> {
                final int id = ((Number) object.get("id")).intValue();
                loader.register(new ParticleImpl(NamespaceID.from(namespace), id));
            });

    static Particle get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Particle getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Particle getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Particle> values() {
        return CONTAINER.values();
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

    @Override
    public String toString() {
        return name();
    }
}
