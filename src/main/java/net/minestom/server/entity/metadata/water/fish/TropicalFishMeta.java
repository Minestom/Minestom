package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public class TropicalFishMeta extends AbstractFishMeta {
    public TropicalFishMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents} instead.
     */
    public @NotNull Variant getVariant() {
        return Variant.fromPackedId(metadata.get(MetadataDef.TropicalFish.VARIANT));
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents} instead.
     */
    public void setVariant(@NotNull Variant variant) {
        metadata.set(MetadataDef.TropicalFish.VARIANT, variant.packedId());
    }

    public record Variant(@NotNull Pattern pattern, @NotNull DyeColor bodyColor, @NotNull DyeColor patternColor) {
        public static final Variant DEFAULT = new Variant(Pattern.KOB, DyeColor.WHITE, DyeColor.WHITE);

        public static @NotNull Variant fromPackedId(int packedId) {
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
                    | (bodyColor.ordinal() << 16)
                    | pattern.id();
        }

        public @NotNull Variant withPattern(@NotNull Pattern newPattern) {
            return new Variant(newPattern, this.bodyColor, this.patternColor);
        }

        public @NotNull Variant withBodyColor(@NotNull DyeColor newBodyColor) {
            return new Variant(this.pattern, newBodyColor, this.patternColor);
        }

        public @NotNull Variant withPatternColor(@NotNull DyeColor newPatternColor) {
            return new Variant(this.pattern, this.bodyColor, newPatternColor);
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
        public static final BinaryTagSerializer<Pattern> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Pattern.class);

        private final static Pattern[] VALUES = values();

        public static @NotNull Pattern fromId(int id) {
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
