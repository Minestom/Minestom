package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PigMeta extends AnimalMeta {

    public PigMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isHasSaddle() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setHasSaddle(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

    public int getTimeToBoost() {
        return getMetadata().getIndex((byte) 17, 0);
    }

    public void setTimeToBoost(int value) {
        getMetadata().getIndex((byte) 17, Metadata.VarInt(value));
    }

}
