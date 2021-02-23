package net.minestom.server.particle;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Particle {

    private ParticleType type;
    private Consumer<BinaryWriter> dataWriter;

    public Particle(@NotNull ParticleType type, @Nullable Consumer<BinaryWriter> dataWriter) {
        this.type = type;
        this.dataWriter = dataWriter;
    }

    @NotNull
    public ParticleType getType() {
        return this.type;
    }

    public void setType(@NotNull ParticleType type) {
        this.type = type;
    }

    @Nullable
    public Consumer<BinaryWriter> getDataWriter() {
        return this.dataWriter;
    }

    public void setDataWriter(@Nullable Consumer<BinaryWriter> dataWriter) {
        this.dataWriter = dataWriter;
    }
}
