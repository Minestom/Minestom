package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WolfMeta extends TameableAnimalMeta {

    public WolfMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isBegging() {
        return super.metadata.getIndex((byte) 18, false);
    }

    public void setBegging(boolean value) {
        super.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return super.metadata.getIndex((byte) 19, 14);
    }

    public void setCollarColor(int value) {
        super.metadata.setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getAngerTime() {
        return super.metadata.getIndex((byte) 20, 0);
    }

    public void setAngerTime(int value) {
        super.metadata.setIndex((byte) 20, Metadata.VarInt(value));
    }

}
