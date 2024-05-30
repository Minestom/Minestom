package net.minestom.server.particle;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class EntityEffectParticle extends ParticleImpl {
    private final @NotNull RGBLike color;

    EntityEffectParticle(@NotNull NamespaceID namespace, int id, @NotNull RGBLike color) {
        super(namespace, id);
        this.color = color;
    }

    @Contract(pure = true)
    public @NotNull EntityEffectParticle withColor(@NotNull RGBLike color) {
        return new EntityEffectParticle(namespace(), id(), color);
    }

    public @NotNull RGBLike color() {
        return color;
    }

    @Override
    public @NotNull EntityEffectParticle readData(@NotNull NetworkBuffer reader) {
        return withColor(reader.read(Color.NETWORK_TYPE));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(Color.NETWORK_TYPE, color);
    }
}
