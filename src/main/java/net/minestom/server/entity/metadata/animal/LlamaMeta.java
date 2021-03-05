package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class LlamaMeta extends ChestedHorseMeta {

    public LlamaMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getStrength() {
        return super.metadata.getIndex((byte) 19, 0);
    }

    public void setStrength(int value) {
        super.metadata.setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getCarpetColor() {
        return super.metadata.getIndex((byte) 20, -1);
    }

    public void setCarpetColor(int value) {
        super.metadata.setIndex((byte) 20, Metadata.VarInt(value));
    }

    public Variant getVariant() {
        return Variant.VALUES[super.metadata.getIndex((byte) 21, 0)];
    }

    public void setVariant(Variant value) {
        super.metadata.setIndex((byte) 21, Metadata.VarInt(value.ordinal()));
    }

    public enum Variant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        private final static Variant[] VALUES = values();
    }

}
