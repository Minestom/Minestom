package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PufferfishMeta extends AbstractFishMeta {
    public static final byte OFFSET = AbstractFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public PufferfishMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
        updateBoundingBox(State.UNPUFFED);
    }

    public State getState() {
        return State.VALUES[super.metadata.getIndex(OFFSET, 0)];
    }

    public void setState(State state) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(state.ordinal()));
        updateBoundingBox(state);
    }

    private void updateBoundingBox(State state) {
        this.consumeEntity((entity) -> {
            switch (state) {
                case UNPUFFED -> entity.setBoundingBox(.35D, .35D, .35D);
                case SEMI_PUFFED -> entity.setBoundingBox(.5D, .5D, .5D);
                default -> entity.setBoundingBox(.7D, .7D, .7D);
            }
        });
    }

    public enum State {
        UNPUFFED,
        SEMI_PUFFED,
        FULLY_PUFFED;

        private final static State[] VALUES = values();
    }

}
