package net.minestom.server.entity.metadata.animal;

import net.minestom.server.codec.Codec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class RabbitMeta extends AnimalMeta {
    public RabbitMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#RABBIT_VARIANT} instead.
     */
    @Deprecated
    public void setVariant(@NotNull RabbitMeta.Variant variant) {
        int id = variant == Variant.KILLER_BUNNY ? 99 : variant.ordinal();
        metadata.set(MetadataDef.Rabbit.TYPE, id);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#RABBIT_VARIANT} instead.
     */
    @Deprecated
    public @NotNull RabbitMeta.Variant getVariant() {
        int id = metadata.get(MetadataDef.Rabbit.TYPE);
        if (id == 99) {
            return Variant.KILLER_BUNNY;
        }
        return Variant.VALUES[id];
    }

    public enum Variant {
        BROWN,
        WHITE,
        BLACK,
        BLACK_AND_WHITE,
        GOLD,
        SALT_AND_PEPPER,
        KILLER_BUNNY;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
        public static final Codec<Variant> CODEC = Codec.Enum(Variant.class);

        private final static Variant[] VALUES = values();
    }

}
