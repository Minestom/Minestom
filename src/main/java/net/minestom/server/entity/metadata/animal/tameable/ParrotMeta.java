package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ParrotMeta extends TameableAnimalMeta {
    public static final byte OFFSET = TameableAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public ParrotMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Color getColor() {
        return Color.VALUES[super.metadata.getIndex(OFFSET, 0)];
    }

    public void setColor(@NotNull Color value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value.ordinal()));
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
