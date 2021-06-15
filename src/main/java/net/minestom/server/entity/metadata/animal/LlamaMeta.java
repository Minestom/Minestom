package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class LlamaMeta extends ChestedHorseMeta {
    public static final byte OFFSET = ChestedHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public LlamaMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getStrength() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setStrength(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getCarpetColor() {
        return super.metadata.getIndex(OFFSET + 1, -1);
    }

    public void setCarpetColor(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public Variant getVariant() {
        return Variant.VALUES[super.metadata.getIndex(OFFSET + 2, 0)];
    }

    public void setVariant(Variant value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value.ordinal()));
    }

    public enum Variant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        private final static Variant[] VALUES = values();
    }

}
