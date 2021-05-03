package net.minestom.server.particle.data;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParticleEffect {

    public void write(BinaryWriter writer) {}
    public @Nullable ParticleEffect read(@Nullable String data) {
        return this;
    }

    public static @NotNull ParticleBuilder builder() {
        return new ParticleBuilder();
    }
}
