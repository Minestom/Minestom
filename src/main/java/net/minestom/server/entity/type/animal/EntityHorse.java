package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityHorse extends EntityAbstractHorse {

    public EntityHorse(@NotNull Position spawnPosition) {
        super(EntityType.HORSE, spawnPosition);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

    public EntityHorse(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.HORSE, spawnPosition, instance);
        setBoundingBox(1.3965D, 1.6D, 1.3965D);
    }

    public Variant getVariant() {
        return getVariantFromID(this.metadata.getIndex((byte) 18, 0));
    }

    public void setVariant(Variant variant) {
        this.metadata.setIndex((byte) 18, Metadata.VarInt(getVariantID(variant.marking, variant.color)));
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
