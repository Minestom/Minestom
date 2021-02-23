package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CatMeta extends TameableAnimalMeta {

    public CatMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public Color getColor() {
        return Color.VALUES[getMetadata().getIndex((byte) 18, 1)];
    }

    public void setColor(@NotNull Color value) {
        getMetadata().setIndex((byte) 18, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLying() {
        return getMetadata().getIndex((byte) 19, false);
    }

    public void setLying(boolean value) {
        getMetadata().setIndex((byte) 19, Metadata.Boolean(value));
    }

    public boolean isRelaxed() {
        return getMetadata().getIndex((byte) 20, false);
    }

    public void setRelaxed(boolean value) {
        getMetadata().setIndex((byte) 20, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return getMetadata().getIndex((byte) 21, 14);
    }

    public void setCollarColor(int value) {
        getMetadata().setIndex((byte) 21, Metadata.VarInt(value));
    }

    public enum Color {
        TABBY,
        BLACK,
        RED,
        SIAMESE,
        BRITISH_SHORTHAIR,
        CALICO,
        PERSIAN,
        RAGDOLL,
        WHITE,
        JELLIE,
        ALL_BLACK;

        private final static Color[] VALUES = values();
    }

}
