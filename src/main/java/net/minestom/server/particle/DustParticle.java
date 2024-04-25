package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DustParticle extends ParticleImpl {
    private final @NotNull Color color;
    private final float scale;

    DustParticle(@NotNull NamespaceID namespace, int id, @NotNull Color color, float scale) {
        super(namespace, id);
        this.color = color;
        this.scale = scale;
    }

    @Contract (pure = true)
    public @NotNull DustParticle withProperties(@NotNull Color color, float scale) {
        return new DustParticle(namespace(), id(), color, scale);
    }

    @Contract(pure = true)
    public @NotNull DustParticle withColor(@NotNull Color color) {
        return this.withProperties(color, scale);
    }

    public @NotNull Color color() {
        return color;
    }

    @Contract(pure = true)
    public @NotNull DustParticle withScale(float scale) {
        return this.withProperties(color, scale);
    }

    public float scale() {
        return scale;
    }

    @Override
    public @NotNull DustParticle readData(@NotNull NetworkBuffer reader) {
        return this.withProperties(new Color(
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255)
        ), reader.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, color.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.blue() / 255f);
        writer.write(NetworkBuffer.FLOAT, scale);
    }
}
