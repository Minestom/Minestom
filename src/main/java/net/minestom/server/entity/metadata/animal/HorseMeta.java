package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class HorseMeta extends AbstractHorseMeta {
    public static final byte OFFSET = AbstractHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public HorseMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Variant getVariant() {
        return getVariantFromID(super.metadata.getIndex(OFFSET, 0));
    }

    public void setVariant(Variant variant) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(getVariantID(variant.marking, variant.color)));
    }

    public static int getVariantID(@NotNull Marking marking, @NotNull Color color) {
        return (marking.ordinal() << 8) + color.ordinal();
    }

    public static Variant getVariantFromID(int variantID) {
        return new Variant(
                Marking.VALUES[variantID >> 8],
                Color.VALUES[variantID & 0xFF]
        );
    }

    public static class Variant {

        private Marking marking;
        private Color color;

        public Variant(@NotNull Marking marking, @NotNull Color color) {
            this.marking = marking;
            this.color = color;
        }

        @NotNull
        public Marking getMarking() {
            return this.marking;
        }

        public void setMarking(@NotNull Marking marking) {
            this.marking = marking;
        }

        @NotNull
        public Color getColor() {
            return this.color;
        }

        public void setColor(@NotNull Color color) {
            this.color = color;
        }

    }

    public enum Marking {
        NONE,
        WHITE,
        WHITE_FIELD,
        WHITE_DOTS,
        BLACK_DOTS;

        private final static Marking[] VALUES = values();
    }

    public enum Color {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN;

        private final static Color[] VALUES = values();
    }

}
