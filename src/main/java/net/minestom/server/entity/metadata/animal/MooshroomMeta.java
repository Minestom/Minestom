package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MooshroomMeta extends CowMeta {

    public MooshroomMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public Variant getVariant() {
        return Variant.valueOf(getMetadata().getIndex((byte) 16, "red").toUpperCase(Locale.ROOT));
    }

    public void setVariant(@NotNull Variant value) {
        getMetadata().setIndex((byte) 16, Metadata.String(value.name().toLowerCase(Locale.ROOT)));
    }

    public enum Variant {
        RED,
        BROWN;
    }

}
