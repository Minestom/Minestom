package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class ArmadilloMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public ArmadilloMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public State getState() {
        return super.metadata.getIndex(OFFSET, State.IDLE);
    }

    public void setState(@NotNull State value) {
        super.metadata.setIndex(OFFSET, Metadata.ArmadilloState(value));
    }

    public enum State {
        IDLE,
        ROLLING,
        SCARED,
        UNROLLING;

        public static final NetworkBuffer.Type<State> NETWORK_TYPE = NetworkBuffer.Enum(State.class);
    }
}
