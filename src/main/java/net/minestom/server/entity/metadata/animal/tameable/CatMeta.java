package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CatMeta extends TameableAnimalMeta {
    public static final byte OFFSET = TameableAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public CatMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public CatMeta.Variant getVariant() {
        return super.metadata.getIndex(OFFSET, Variant.BLACK);
    }

    public void setVariant(@NotNull CatMeta.Variant value) {
        super.metadata.setIndex(OFFSET, Metadata.CatVariant(value));
    }

    public boolean isLying() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setLying(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isRelaxed() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setRelaxed(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

    public @NotNull DyeColor getCollarColor() {
        return DYE_VALUES[super.metadata.getIndex(OFFSET + 3, DyeColor.RED.ordinal())];
    }

    public void setCollarColor(@NotNull DyeColor value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value.ordinal()));
    }

    public enum Variant {
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

        private static final Variant[] VALUES = values();
    }

}
