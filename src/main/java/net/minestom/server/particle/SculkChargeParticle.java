package net.minestom.server.particle;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class SculkChargeParticle extends ParticleImpl {
    private final float roll;

    SculkChargeParticle(@NotNull NamespaceID namespace, int id, float roll) {
        super(namespace, id);
        this.roll = roll;
    }

    @Contract(pure = true)
    public @NotNull SculkChargeParticle withRoll(float roll) {
        return new SculkChargeParticle(namespace(), id(), roll);
    }

    public float roll() {
        return roll;
    }

    @Override
    public @NotNull SculkChargeParticle readData(@NotNull NetworkBuffer reader) {
        return this.withRoll(reader.read(NetworkBuffer.FLOAT));
    }

    @Override
    public void writeData(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.FLOAT, roll);

    }
}
