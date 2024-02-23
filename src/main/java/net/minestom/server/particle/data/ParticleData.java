package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public interface ParticleData {
    void write(@NotNull NetworkBuffer writer);
}
