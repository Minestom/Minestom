package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class StriderMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public StriderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getTimeToBoost() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setTimeToBoost(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public boolean isShaking() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setShaking(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isHasSaddle() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setHasSaddle(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

}
