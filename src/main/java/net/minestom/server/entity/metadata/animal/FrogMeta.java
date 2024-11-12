package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrogMeta extends AnimalMeta {
    public FrogMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Variant getVariant() {
        return metadata.get(MetadataDef.Frog.VARIANT);
    }

    public void setVariant(@NotNull Variant value) {
        metadata.set(MetadataDef.Frog.VARIANT, value);
    }

    public @Nullable Integer getTongueTarget() {
        return metadata.get(MetadataDef.Frog.TONGUE_TARGET);
    }

    public void setTongueTarget(@Nullable Integer value) {
        metadata.set(MetadataDef.Frog.TONGUE_TARGET, value);
    }

    public enum Variant {
        TEMPERATE,
        WARM,
        COLD;

        public static final NetworkBuffer.Type<Variant> NETWORK_TYPE = NetworkBuffer.Enum(Variant.class);
    }
}
