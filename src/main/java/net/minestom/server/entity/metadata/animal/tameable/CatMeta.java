package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class CatMeta extends TameableAnimalMeta {
    private static final DyeColor[] DYE_VALUES = DyeColor.values();

    public CatMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public CatMeta.Variant getVariant() {
        return metadata.get(MetadataDef.Cat.VARIANT);
    }

    public void setVariant(@NotNull CatMeta.Variant value) {
        metadata.set(MetadataDef.Cat.VARIANT, value);
    }

    public boolean isLying() {
        return metadata.get(MetadataDef.Cat.IS_LYING);
    }

    public void setLying(boolean value) {
        metadata.set(MetadataDef.Cat.IS_LYING, value);
    }

    public boolean isRelaxed() {
        return metadata.get(MetadataDef.Cat.IS_RELAXED);
    }

    public void setRelaxed(boolean value) {
        metadata.set(MetadataDef.Cat.IS_RELAXED, value);
    }

    public @NotNull DyeColor getCollarColor() {
        return DYE_VALUES[metadata.get(MetadataDef.Cat.COLLAR_COLOR)];
    }

    public void setCollarColor(@NotNull DyeColor value) {
        metadata.set(MetadataDef.Cat.COLLAR_COLOR, value.ordinal());
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

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
    }

}
