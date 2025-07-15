package net.minestom.server.entity.metadata.water;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.animal.AnimalMeta;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

public class AxolotlMeta extends AnimalMeta {
    public AxolotlMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#AXOLOTL_VARIANT} instead.
     */
    @Deprecated
    public Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Axolotl.VARIANT)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#AXOLOTL_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(Variant variant) {
        metadata.set(MetadataDef.Axolotl.VARIANT, variant.ordinal());
    }

    public boolean isPlayingDead() {
        return metadata.get(MetadataDef.Axolotl.IS_PLAYING_DEAD);
    }

    public void setPlayingDead(boolean playingDead) {
        metadata.set(MetadataDef.Axolotl.IS_PLAYING_DEAD, playingDead);
    }

    public boolean isFromBucket() {
        return metadata.get(MetadataDef.Axolotl.IS_FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        metadata.set(MetadataDef.Axolotl.IS_FROM_BUCKET, fromBucket);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.AXOLOTL_VARIANT)
            return (T) getVariant();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.AXOLOTL_VARIANT)
            setVariant((Variant) value);
        else super.set(component, value);
    }

    public enum Variant {
        LUCY,
        WILD,
        GOLD,
        CYAN,
        BLUE;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
        public static final Codec<Variant> CODEC = Codec.Enum(Variant.class);

        private final static AxolotlMeta.Variant[] VALUES = values();
    }
}
