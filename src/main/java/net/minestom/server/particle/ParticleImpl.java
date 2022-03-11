package net.minestom.server.particle;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record ParticleImpl(NamespaceID namespace, int id) implements Particle {
    private static final Registry.Container<Particle> CONTAINER = Registry.createContainer(Registry.Resource.PARTICLES,
            (namespace, properties) -> new ParticleImpl(NamespaceID.from(namespace), properties.getInt("id")));

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

    @Override
    public String toString() {
        return name();
    }
}
