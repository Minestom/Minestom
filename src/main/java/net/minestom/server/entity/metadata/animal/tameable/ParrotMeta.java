package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ParrotMeta extends TameableAnimalMeta {

    public ParrotMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Color getColor() {
        return Color.VALUES[super.metadata.getIndex((byte) 18, 0)];
    }

    public void setColor(@NotNull Color value) {
        super.metadata.setIndex((byte) 18, Metadata.VarInt(value.ordinal()));
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
