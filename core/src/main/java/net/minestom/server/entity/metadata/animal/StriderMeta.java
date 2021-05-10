package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class StriderMeta extends AnimalMeta {

    public StriderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getTimeToBoost() {
        return super.metadata.getIndex((byte) 16, 0);
    }

    public void setTimeToBoost(int value) {
        super.metadata.setIndex((byte) 16, Metadata.VarInt(value));
    }

    public boolean isShaking() {
        return super.metadata.getIndex((byte) 17, false);
    }

    public void setShaking(boolean value) {
        super.metadata.setIndex((byte) 17, Metadata.Boolean(value));
    }

    public boolean isHasSaddle() {
        return super.metadata.getIndex((byte) 18, false);
    }

    public void setHasSaddle(boolean value) {
        super.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

}
