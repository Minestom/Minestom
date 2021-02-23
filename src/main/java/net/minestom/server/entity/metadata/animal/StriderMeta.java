package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class StriderMeta extends AnimalMeta {

    public StriderMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getTimeToBoost() {
        return getMetadata().getIndex((byte) 16, 0);
    }

    public void setTimeToBoost(int value) {
        getMetadata().setIndex((byte) 16, Metadata.VarInt(value));
    }

    public boolean isShaking() {
        return getMetadata().getIndex((byte) 17, false);
    }

    public void setShaking(boolean value) {
        getMetadata().setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isHasSaddle() {
        return getMetadata().getIndex((byte) 18, false);
    }

    public void setHasSaddle(boolean value) {
        getMetadata().setIndex((byte) 18, Metadata.Boolean(value));
    }

}
