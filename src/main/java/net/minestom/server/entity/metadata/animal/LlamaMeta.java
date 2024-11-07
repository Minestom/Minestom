package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class LlamaMeta extends ChestedHorseMeta {
    public LlamaMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getStrength() {
        return metadata.get(MetadataDef.Llama.STRENGTH);
    }

    public void setStrength(int value) {
        metadata.set(MetadataDef.Llama.STRENGTH, value);
    }

    public int getCarpetColor() {
        return metadata.get(MetadataDef.Llama.CARPET_COLOR);
    }

    public void setCarpetColor(int value) {
        metadata.set(MetadataDef.Llama.CARPET_COLOR, value);
    }

    public Variant getVariant() {
        return Variant.VALUES[metadata.get(MetadataDef.Llama.VARIANT)];
    }

    public void setVariant(Variant value) {
        metadata.set(MetadataDef.Llama.VARIANT, value.ordinal());
    }

    public enum Variant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        private final static Variant[] VALUES = values();
    }

}
