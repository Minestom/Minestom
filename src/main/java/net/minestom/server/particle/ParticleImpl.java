package net.minestom.server.particle;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

final class ParticleImpl implements Particle {
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
