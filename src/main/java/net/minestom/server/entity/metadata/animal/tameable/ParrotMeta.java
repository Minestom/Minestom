package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class ParrotMeta extends TameableAnimalMeta {
    public ParrotMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Color getColor() {
        return Color.VALUES[metadata.get(MetadataDef.Parrot.VARIANT)];
    }

    public void setColor(@NotNull Color value) {
        metadata.set(MetadataDef.Parrot.VARIANT, value.ordinal());
    }

    public enum Color {
        RED_BLUE,
        BLUE,
        GREEN,
        YELLOW_BLUE,
        GREY;

        private final static Color[] VALUES = values();
    }

}
