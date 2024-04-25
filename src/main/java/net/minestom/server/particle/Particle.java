package net.minestom.server.particle;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Particle extends StaticProtocolObject, Particles permits ParticleImpl {
    static @NotNull Collection<@NotNull Particle> values() {
        return ParticleImpl.values();
    }

    static @Nullable Particle fromNamespaceId(@NotNull String namespaceID) {
        return ParticleImpl.getSafe(namespaceID);
    }

    static @Nullable Particle fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Particle fromId(int id) {
        return ParticleImpl.getId(id);
    }

    @NotNull Particle readData(@NotNull NetworkBuffer reader);

    void writeData(@NotNull NetworkBuffer writer);
}
