package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.codec.Codec;
import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

public class TropicalFishMeta extends AbstractFishMeta {
    public TropicalFishMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents} instead.
     */
    @Deprecated
    public Variant getVariant() {
        return Variant.fromPackedId(metadata.get(MetadataDef.TropicalFish.VARIANT));
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents} instead.
     */
    @Deprecated
    public void setVariant(Variant variant) {
        metadata.set(MetadataDef.TropicalFish.VARIANT, variant.packedId());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.TROPICAL_FISH_PATTERN)
            return (T) getVariant().pattern();
        if (component == DataComponents.TROPICAL_FISH_BASE_COLOR)
            return (T) getVariant().baseColor();
        if (component == DataComponents.TROPICAL_FISH_PATTERN_COLOR)
            return (T) getVariant().patternColor();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.TROPICAL_FISH_PATTERN)
            setVariant(getVariant().withPattern((Pattern) value));
        else if (component == DataComponents.TROPICAL_FISH_BASE_COLOR)
            setVariant(getVariant().withBodyColor((DyeColor) value));
        else if (component == DataComponents.TROPICAL_FISH_PATTERN_COLOR)
            setVariant(getVariant().withPatternColor((DyeColor) value));
        else super.set(component, value);
    }

    public record Variant(Pattern pattern, DyeColor baseColor, DyeColor patternColor) {
        public static final Variant DEFAULT = new Variant(Pattern.KOB, DyeColor.WHITE, DyeColor.WHITE);

        public static Variant fromPackedId(int packedId) {
            int patternColorId = (packedId >> 24) & 0xFF;
            int bodyColorId = (packedId >> 16) & 0xFF;
            int patternId = packedId & 0xFF;

            DyeColor patternColor = DyeColor.values()[patternColorId];
            DyeColor bodyColor = DyeColor.values()[bodyColorId];
            Pattern pattern = Pattern.fromId(patternId);

            return new Variant(pattern, bodyColor, patternColor);
        }

        public int packedId() {
            return (patternColor.ordinal() << 24)
                    | (baseColor.ordinal() << 16)
                    | pattern.id();
        }

        public Variant withPattern(Pattern newPattern) {
            return new Variant(newPattern, this.baseColor, this.patternColor);
        }

        public Variant withBodyColor(DyeColor newBodyColor) {
            return new Variant(this.pattern, newBodyColor, this.patternColor);
        }

        public Variant withPatternColor(DyeColor newPatternColor) {
            return new Variant(this.pattern, this.baseColor, newPatternColor);
        }
    }

    public enum Pattern {
        KOB(false, 0),
        SUNSTREAK(false, 1),
        SNOOPER(false, 2),
        DASHER(false, 3),
        BRINELY(false, 4),
        SPOTTY(false, 5),
        FLOPPER(true, 0),
        STRIPEY(true, 1),
        GLITTER(true, 2),
        BLOCKFISH(true, 3),
        BETTY(true, 4),
        CLAYFISH(true, 5);

        public static final NetworkBuffer.Type<Pattern> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(Pattern::fromId, Pattern::id);
        public static final Codec<Pattern> CODEC = Codec.Enum(Pattern.class);

        private final static Pattern[] VALUES = values();

        public static Pattern fromId(int id) {
            for (Pattern pattern : VALUES) {
                if (pattern.id() == id) {
                    return pattern;
                }
            }
            throw new IllegalArgumentException("Invalid pattern id: " + id);
        }

        private final int id;

        Pattern(boolean isLarge, int id) {
            this.id = (isLarge ? 1 : 0) | (id << 8);
        }

        public int id() {
            return this.id;
        }
    }

}
