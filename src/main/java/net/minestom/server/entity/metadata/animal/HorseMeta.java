package net.minestom.server.entity.metadata.animal;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

public class HorseMeta extends AbstractHorseMeta {
    public HorseMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#HORSE_VARIANT} instead.
     */
    @Deprecated
    public Variant getVariant() {
        return getVariantFromID(metadata.get(MetadataDef.Horse.VARIANT));
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#HORSE_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(Variant variant) {
        metadata.set(MetadataDef.Horse.VARIANT, getVariantID(variant.marking, variant.color));
    }

    public static int getVariantID(Marking marking, Color color) {
        return (marking.ordinal() << 8) + color.ordinal();
    }

    public static Variant getVariantFromID(int variantID) {
        return new Variant(
                Marking.VALUES[variantID >> 8],
                Color.VALUES[variantID & 0xFF]
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.HORSE_VARIANT)
            return (T) getVariant().getMarking();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.HORSE_VARIANT) {
            var variant = getVariant();
            variant.setMarking((Marking) value);
            setVariant(variant);
        } else super.set(component, value);
    }

    public static class Variant {

        private Marking marking;
        private Color color;

        public Variant(Marking marking, Color color) {
            this.marking = marking;
            this.color = color;
        }

            public Marking getMarking() {
            return this.marking;
        }

        public void setMarking(Marking marking) {
            this.marking = marking;
        }

            public Color getColor() {
            return this.color;
        }

        public void setColor(Color color) {
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

        public static final NetworkBuffer.Type<Color> NETWORK_TYPE = NetworkBuffer.Enum(Color.class);
        public static final Codec<Color> NBT_TYPE = Codec.Enum(Color.class);

        private final static Color[] VALUES = values();
    }

}
