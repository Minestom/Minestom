package net.minestom.server.entity.type.water;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityPufferFish extends EntityAbstractFish {

    public EntityPufferFish(@NotNull Position spawnPosition) {
        super(EntityType.PUFFERFISH, spawnPosition);
        updateBoundingBox(State.UNPUFFED);
    }

    public EntityPufferFish(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.PUFFERFISH, spawnPosition, instance);
        updateBoundingBox(State.UNPUFFED);
    }

    public State getState() {
        return State.VALUES[this.metadata.getIndex((byte) 16, 0)];
    }

    public void setState(State state) {
        this.metadata.setIndex((byte) 16, Metadata.VarInt(state.ordinal()));
        updateBoundingBox(state);
    }

    private void updateBoundingBox(State state) {
        switch (state) {
            case UNPUFFED:
                setBoundingBox(.35D, .35D, .35D);
                break;
            case SEMI_PUFFED:
                setBoundingBox(.5D, .5D, .5D);
                break;
            default:
                setBoundingBox(.7D, .7D, .7D);
                break;
        }
    }

    public enum State {
        UNPUFFED,
        SEMI_PUFFED,
        FULLY_PUFFED;

        private final static State[] VALUES = values();
    }

}
