package net.minestom.server.particle;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ShriekParticle extends ParticleImpl {
    private final int delay;

    ShriekParticle(@NotNull NamespaceID namespace, int id, int delay) {
        super(namespace, id);
        this.delay = delay;
    }

    @Contract(pure = true)
    public @NotNull ShriekParticle withDelay(int delay) {
        return new ShriekParticle(namespace(), id(), delay);
    }

    public int delay() {
        return delay;
    }

    @Override
    public @NotNull ShriekParticle readData(@NotNull NetworkBuffer reader) {
        return this.withDelay(reader.read(NetworkBuffer.VAR_INT));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.VAR_INT, delay);
    }
}
