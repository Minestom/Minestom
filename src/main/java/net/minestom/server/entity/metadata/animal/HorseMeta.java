package net.minestom.server.entity.metadata.animal;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

public class HorseMeta extends AbstractHorseMeta {
    public HorseMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Horse.VARIANT) & 0xFF];
    }

    public void setVariant(Variant variant) {
        metadata.set(MetadataDef.Horse.VARIANT, variant.ordinal() | (metadata.get(MetadataDef.Horse.VARIANT) & ~0xFF));
    }

    public Marking getMarking() {
        return Marking.VALUES[(metadata.get(MetadataDef.Horse.VARIANT) >> 8) & 0xFF];
    }

    public void setMarking(Marking marking) {
        metadata.set(MetadataDef.Horse.VARIANT, (metadata.get(MetadataDef.Horse.VARIANT) & 0xFF) | (marking.ordinal() << 8));
    }

    public void setVariantAndMarking(Variant variant, Marking marking) {
        metadata.set(MetadataDef.Horse.VARIANT, variant.ordinal() | (marking.ordinal() << 8));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.HORSE_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.HORSE_VARIANT) {
            setVariant((Variant) value);
        } else {
            super.set(component, value);
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

    public enum Variant {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
        public static final Codec<Variant> CODEC = Codec.Enum(Variant.class);

        private final static Variant[] VALUES = values();
    }

}
