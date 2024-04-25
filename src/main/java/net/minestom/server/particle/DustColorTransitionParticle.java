package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DustColorTransitionParticle extends ParticleImpl {
    private final @NotNull Color color;
    private final float scale;
    private final @NotNull Color transitionColor;

    DustColorTransitionParticle(@NotNull NamespaceID namespace, int id, @NotNull Color color, float scale, @NotNull Color transitionColor) {
        super(namespace, id);
        this.color = color;
        this.scale = scale;
        this.transitionColor = transitionColor;
    }

    @Contract (pure = true)
    public @NotNull DustColorTransitionParticle withProperties(@NotNull Color color, float scale, @NotNull Color transitionColor) {
        return new DustColorTransitionParticle(namespace(), id(), color, scale, transitionColor);
    }

    @Contract(pure = true)
    public @NotNull DustColorTransitionParticle withColor(@NotNull Color color) {
        return this.withProperties(color, scale, transitionColor);
    }

    public @NotNull Color color() {
        return color;
    }

    @Contract(pure = true)
    public @NotNull DustColorTransitionParticle withScale(float scale) {
        return this.withProperties(color, scale, transitionColor);
    }

    public float scale() {
        return scale;
    }

    @Contract(pure = true)
    public @NotNull DustColorTransitionParticle withTransitionColor(@NotNull Color transitionColor) {
        return this.withProperties(color, scale, transitionColor);
    }

    public @NotNull Color transitionColor() {
        return color;
    }

    @Override
    public @NotNull DustColorTransitionParticle readData(@NotNull NetworkBuffer reader) {
        return this.withProperties(new Color(
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255)
        ), reader.read(NetworkBuffer.FLOAT), new Color(
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255),
                (int) (reader.read(NetworkBuffer.FLOAT) * 255)
        ));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, color.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, color.blue() / 255f);
        writer.write(NetworkBuffer.FLOAT, scale);
        writer.write(NetworkBuffer.FLOAT, transitionColor.red() / 255f);
        writer.write(NetworkBuffer.FLOAT, transitionColor.green() / 255f);
        writer.write(NetworkBuffer.FLOAT, transitionColor.blue() / 255f);
    }
}
