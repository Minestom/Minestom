package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WolfMeta extends TameableAnimalMeta {

    public WolfMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isBegging() {
        return getMetadata().getIndex((byte) 18, false);
    }

    public void setBegging(boolean value) {
        getMetadata().setIndex((byte) 18, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return getMetadata().getIndex((byte) 19, 14);
    }

    public void setCollarColor(int value) {
        getMetadata().setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getAngerTime() {
        return getMetadata().getIndex((byte) 20, 0);
    }

    public void setAngerTime(int value) {
        getMetadata().setIndex((byte) 20, Metadata.VarInt(value));
    }

}
