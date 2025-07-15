package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

public class ParrotMeta extends TameableAnimalMeta {
    public ParrotMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PARROT_VARIANT} instead.
     */
    @Deprecated
    public Color getColor() {
        return Color.VALUES[metadata.get(MetadataDef.Parrot.VARIANT)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PARROT_VARIANT} instead.
     */
    @Deprecated
    public void setColor(Color value) {
        metadata.set(MetadataDef.Parrot.VARIANT, value.ordinal());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.PARROT_VARIANT)
            return (T) getColor();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.PARROT_VARIANT)
            setColor((Color) value);
        else super.set(component, value);
    }

    public enum Color {
        RED_BLUE,
        BLUE,
        GREEN,
        YELLOW_BLUE,
        GREY;

        public static final NetworkBuffer.Type<Color> NETWORK_TYPE = NetworkBuffer.Enum(Color.class);
        public static final Codec<Color> CODEC = Codec.Enum(Color.class);

        private final static Color[] VALUES = values();
    }

}
