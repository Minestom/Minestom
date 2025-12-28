package net.minestom.server.world.biome;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.Color;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public record BiomeEffects(
        RGBLike waterColor,
        @Nullable RGBLike foliageColor,
        @Nullable RGBLike dryFoliageColor,
        @Nullable RGBLike grassColor,
        GrassColorModifier grassColorModifier
) {
    public static final BiomeEffects DEFAULT = new BiomeEffects(new Color(0x3f76e4), null, null, null, GrassColorModifier.NONE);

    public static final Codec<BiomeEffects> CODEC = StructCodec.struct(
            "water_color", Color.STRING_CODEC, BiomeEffects::waterColor,
            "foliage_color", Color.STRING_CODEC.optional(), BiomeEffects::foliageColor,
            "dry_foliage_color", Color.STRING_CODEC.optional(), BiomeEffects::dryFoliageColor,
            "grass_color", Color.STRING_CODEC.optional(), BiomeEffects::grassColor,
            "grass_color_modifier", GrassColorModifier.CODEC.optional(GrassColorModifier.NONE), BiomeEffects::grassColorModifier,
            BiomeEffects::new);

    public static Builder builder() {
        return new Builder();
    }

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;

        public static final Codec<GrassColorModifier> CODEC = Codec.Enum(GrassColorModifier.class);
    }

    public static final class Builder {
        private RGBLike waterColor = new Color(0x3f76e4);
        private @Nullable RGBLike foliageColor;
        private @Nullable RGBLike dryFoliageColor;
        private @Nullable RGBLike grassColor;
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;

        Builder() {
        }

        @Contract(value = "_ -> this")
        public Builder waterColor(RGBLike waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder foliageColor(@Nullable RGBLike foliageColor) {
            this.foliageColor = foliageColor;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder dryFoliageColor(@Nullable RGBLike dryFoliageColor) {
            this.dryFoliageColor = dryFoliageColor;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder grassColor(@Nullable RGBLike grassColor) {
            this.grassColor = grassColor;
            return this;
        }

        @Contract(value = "_ -> this")
        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        @Contract(pure = true, value = "-> new")
        public BiomeEffects build() {
            Check.argCondition(waterColor == null, "waterColor is required");

            return new BiomeEffects(waterColor, foliageColor, dryFoliageColor, grassColor, grassColorModifier);
        }
    }
}
