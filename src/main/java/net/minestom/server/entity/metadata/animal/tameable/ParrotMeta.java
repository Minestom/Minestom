package net.minestom.server.entity.metadata.animal.tameable;

import net.minestom.server.codec.Codec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class ParrotMeta extends TameableAnimalMeta {
    public ParrotMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PARROT_VARIANT} instead.
     */
    public @NotNull Color getColor() {
        return Color.VALUES[metadata.get(MetadataDef.Parrot.VARIANT)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#PARROT_VARIANT} instead.
     */
    public void setColor(@NotNull Color value) {
        metadata.set(MetadataDef.Parrot.VARIANT, value.ordinal());
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
