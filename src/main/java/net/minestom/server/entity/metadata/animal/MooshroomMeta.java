package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MooshroomMeta extends CowMeta {
    public MooshroomMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Variant getVariant() {
        return Variant.valueOf(metadata.get(MetadataDef.Mooshroom.VARIANT).toUpperCase(Locale.ROOT));
    }

    public void setVariant(@NotNull Variant value) {
        metadata.set(MetadataDef.Mooshroom.VARIANT, value.name().toLowerCase(Locale.ROOT));
    }

    public enum Variant {
        RED,
        BROWN
    }

}
