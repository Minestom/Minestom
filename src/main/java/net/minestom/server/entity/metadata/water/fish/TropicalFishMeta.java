package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;

public class TropicalFishMeta extends AbstractFishMeta implements ObjectDataProvider {
    public static final byte OFFSET = AbstractFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public TropicalFishMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Variant getVariant() {
        return getVariantFromID(super.metadata.getIndex(OFFSET, 0));
    }

    public void setVariant(Variant variant) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(getVariantID(variant)));
    }

    public static int getVariantID(Variant variant) {
        int id = 0;
        id |= variant.patternColor;
        id <<= 8;
        id |= variant.bodyColor;
        id <<= 8;
        id |= variant.pattern.ordinal();
        id <<= 8;
        id |= variant.type.ordinal();
        return id;
    }

    public static Variant getVariantFromID(int variantID) {
        Type type = Type.VALUES[variantID & 0xFF];
        variantID >>= 8;
        Pattern pattern = Pattern.VALUES[variantID & 0xFF];
        variantID >>= 8;
        byte bodyColor = (byte) (variantID & 0xFF);
        variantID >>= 8;
        byte patternColor = (byte) (variantID & 0xFF);
        return new Variant(type, pattern, bodyColor, patternColor);
    }

    @Override
    public int getObjectData() {
        // TODO: returns Entity ID of the owner (???)
        return 0;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }

    public static class Variant {

        private Type type;
        private Pattern pattern;
        private byte bodyColor;
        private byte patternColor;

        public Variant(@NotNull Type type, @NotNull Pattern pattern, byte bodyColor, byte patternColor) {
            this.type = type;
            this.pattern = pattern;
            this.bodyColor = bodyColor;
            this.patternColor = patternColor;
        }

        @NotNull
        public Type getType() {
            return this.type;
        }

        public void setType(@NotNull Type type) {
            this.type = type;
        }

        @NotNull
        public Pattern getPattern() {
            return this.pattern;
        }

        public void setPattern(@NotNull Pattern pattern) {
            this.pattern = pattern;
        }

        public byte getBodyColor() {
            return this.bodyColor;
        }

        public void setBodyColor(byte bodyColor) {
            this.bodyColor = bodyColor;
        }

        public byte getPatternColor() {
            return this.patternColor;
        }

        public void setPatternColor(byte patternColor) {
            this.patternColor = patternColor;
        }
    }

    public enum Type {
        SMALL,
        LARGE,
        INVISIBLE;

        private final static Type[] VALUES = values();
    }

    public enum Pattern {
        KOB, // FLOPPER for LARGE fish
        SUNSTREAK, // STRIPEY for LARGE fish
        SNOOPER, // GLITTER for LARGE fish
        DASHER, // BLOCKFISH for LARGE fish
        BRINELY, // BETTY for LARGE fish
        SPOTTY, // CLAYFISH for LARGE fish
        NONE;

        private final static Pattern[] VALUES = values();
    }

}
