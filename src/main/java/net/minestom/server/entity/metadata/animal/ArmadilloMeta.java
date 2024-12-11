package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class ArmadilloMeta extends AnimalMeta {
    public ArmadilloMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public State getState() {
        return metadata.get(MetadataDef.Armadillo.STATE);
    }

    public void setState(@NotNull State value) {
        metadata.set(MetadataDef.Armadillo.STATE, value);
    }

    public enum State {
        IDLE,
        ROLLING,
        SCARED,
        UNROLLING;

        public static final NetworkBuffer.Type<State> NETWORK_TYPE = NetworkBuffer.Enum(State.class);
    }
}
