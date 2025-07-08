package net.minestom.server.entity.metadata.animal;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MooshroomMeta extends AnimalMeta {
    public MooshroomMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#MOOSHROOM_VARIANT} instead.
     */
    @Deprecated
    public @NotNull Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Mooshroom.VARIANT)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#MOOSHROOM_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull Variant value) {
        metadata.set(MetadataDef.Mooshroom.VARIANT, value.ordinal());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        if (component == DataComponents.MOOSHROOM_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        if (component == DataComponents.MOOSHROOM_VARIANT)
            setVariant((Variant) value);
        else super.set(component, value);
    }

    public enum Variant {
        RED,
        BROWN;

        private static final Variant[] VALUES = values();

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
        public static final Codec<Variant> CODEC = Codec.Enum(Variant.class);
    }

}
