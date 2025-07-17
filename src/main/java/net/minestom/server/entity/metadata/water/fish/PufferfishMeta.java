package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PufferfishMeta extends AbstractFishMeta {
    public PufferfishMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
        updateBoundingBox(State.UNPUFFED);
    }

    public State getState() {
        return State.VALUES[metadata.get(MetadataDef.PufferFish.PUFF_STATE)];
    }

    public void setState(State state) {
        metadata.set(MetadataDef.PufferFish.PUFF_STATE, state.ordinal());
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
