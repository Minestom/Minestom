package net.minestom.server.particle;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @deprecated It's a a temporary solution so that
 * {@link net.minestom.server.entity.metadata.other.AreaEffectCloudMeta} could work.
 */
@Deprecated
public class ParticleWrapper {

    private Particle type;
    private Consumer<BinaryWriter> dataWriter;

    public ParticleWrapper(@NotNull Particle type, @Nullable Consumer<BinaryWriter> dataWriter) {
        this.type = type;
        this.dataWriter = dataWriter;
    }

    @NotNull
    public Particle getType() {
        return this.type;
    }

    public void setType(@NotNull Particle type) {
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
