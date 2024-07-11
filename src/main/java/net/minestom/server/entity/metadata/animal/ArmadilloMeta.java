package net.minestom.server.entity.metadata.animal;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AbstractAgeableMeta;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class ArmadilloMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public ArmadilloMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Override
    public void setBaby(boolean value) {
        if (isBaby() == value) return;
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getEntityType().registry().boundingBox();
            if (value) entity.setBoundingBox(bb.width() * 0.6, bb.height() * 0.6, bb.depth() * 0.6);
            else entity.setBoundingBox(bb);
        });
        super.metadata.setIndex(AbstractAgeableMeta.OFFSET, Metadata.Boolean(value));
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
