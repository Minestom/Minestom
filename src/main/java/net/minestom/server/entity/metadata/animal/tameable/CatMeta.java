package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CatMeta extends TameableAnimalMeta {

    public CatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Color getColor() {
        return Color.VALUES[super.metadata.getIndex((byte) 18, 1)];
    }

    public void setColor(@NotNull Color value) {
        super.metadata.setIndex((byte) 18, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLying() {
        return super.metadata.getIndex((byte) 19, false);
    }

    public void setLying(boolean value) {
        super.metadata.setIndex((byte) 19, Metadata.Boolean(value));
    }

    public boolean isRelaxed() {
        return super.metadata.getIndex((byte) 20, false);
    }

    public void setRelaxed(boolean value) {
        super.metadata.setIndex((byte) 20, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return super.metadata.getIndex((byte) 21, 14);
    }

    public void setCollarColor(int value) {
        super.metadata.setIndex((byte) 21, Metadata.VarInt(value));
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
