package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.animal.EntityLlama;
import org.jetbrains.annotations.NotNull;

public class LlamaMeta extends ChestedHorseMeta {

    public LlamaMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getStrength() {
        return getMetadata().getIndex((byte) 19, 0);
    }

    public void setStrength(int value) {
        getMetadata().setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getCarpetColor() {
        return getMetadata().getIndex((byte) 20, -1);
    }

    public void setCarpetColor(int value) {
        getMetadata().setIndex((byte) 20, Metadata.VarInt(value));
    }

    public Variant getVariant() {
        return Variant.VALUES[getMetadata().getIndex((byte) 21, 0)];
    }

    public void setVariant(EntityLlama.Variant value) {
        getMetadata().setIndex((byte) 21, Metadata.VarInt(value.ordinal()));
    }

    public enum Variant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        private final static Variant[] VALUES = values();
    }

}
