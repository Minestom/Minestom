package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class PufferfishMeta extends AbstractFishMeta {
    public PufferfishMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
        updateBoundingBox(State.UNPUFFED);
    }

    public State getState() {
        return State.VALUES[get(MetadataDef.PufferFish.PUFF_STATE)];
    }

    public void setState(State state) {
        set(MetadataDef.PufferFish.PUFF_STATE, state.ordinal());
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
