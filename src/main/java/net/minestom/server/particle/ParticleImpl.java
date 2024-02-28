package net.minestom.server.particle;

import net.minestom.server.particle.data.ParticleData;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

record ParticleImpl(NamespaceID namespace, int id, ParticleData data) implements Particle {
    private static final Registry.Container<Particle> CONTAINER = Registry.createStaticContainer(Registry.Resource.PARTICLES,
            (namespace, properties) -> new ParticleImpl(NamespaceID.from(namespace), properties.getInt("id"), ParticleData.defaultData(namespace)));

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

    public @NotNull Particle withData(@Nullable ParticleData object) {
        return new ParticleImpl(namespace, id, object);
    }

    @Override
    public @Nullable ParticleData data() {
        return data;
    }

    @Override
    public @NotNull String toString() {
        return name();
    }
}
