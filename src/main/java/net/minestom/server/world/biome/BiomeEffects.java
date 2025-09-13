package net.minestom.server.world.biome;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.Color;
import net.minestom.server.sound.Music;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BiomeEffects(
        RGBLike fogColor,
        RGBLike skyColor,
        RGBLike waterColor,
        RGBLike waterFogColor,
        @Nullable RGBLike foliageColor,
        @Nullable RGBLike grassColor,
        GrassColorModifier grassColorModifier,
        @Nullable BiomeEffects.Particle biomeParticle,
        @Nullable SoundEvent ambientSound,
        @Nullable BiomeEffects.MoodSound moodSound,
        @Nullable BiomeEffects.AdditionsSound additionsSound,
        @Nullable Music music
) {
    public static final BiomeEffects PLAINS_EFFECTS = BiomeEffects.builder()
            .fogColor(new Color(0xC0D8FF))
            .skyColor(new Color(0x78A7FF))
            .waterColor(new Color(0x3F76E4))
            .waterFogColor(new Color(0x50533))
            .build();

    public static final Codec<BiomeEffects> CODEC = StructCodec.struct(
            "fog_color", Color.CODEC, BiomeEffects::fogColor,
            "sky_color", Color.CODEC, BiomeEffects::skyColor,
            "water_color", Color.CODEC, BiomeEffects::waterColor,
            "water_fog_color", Color.CODEC, BiomeEffects::waterFogColor,
            "foliage_color", Color.CODEC.optional(), BiomeEffects::foliageColor,
            "grass_color", Color.CODEC.optional(), BiomeEffects::grassColor,
            "grass_color_modifier", GrassColorModifier.CODEC.optional(GrassColorModifier.NONE), BiomeEffects::grassColorModifier,
            "particle", Particle.CODEC.optional(), BiomeEffects::biomeParticle,
            "ambient_sound", SoundEvent.CODEC.optional(), BiomeEffects::ambientSound,
            "mood_sound", MoodSound.CODEC.optional(), BiomeEffects::moodSound,
            "additions_sound", AdditionsSound.CODEC.optional(), BiomeEffects::additionsSound,
            "music", Music.CODEC.optional(), BiomeEffects::music,
            BiomeEffects::new);

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;

        public static final Codec<GrassColorModifier> CODEC = Codec.Enum(GrassColorModifier.class);
    }

    public record Particle(float probability, net.minestom.server.particle.Particle particle) {
        public static final Codec<Particle> CODEC = StructCodec.struct(
                "probability", Codec.FLOAT, Particle::probability,
                "options", net.minestom.server.particle.Particle.CODEC, Particle::particle,
                Particle::new);
    }

    public record MoodSound(
            SoundEvent sound,
            int tickDelay,
            int blockSearchExtent,
            double offset
    ) {
        public static final Codec<MoodSound> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, MoodSound::sound,
                "tick_delay", Codec.INT, MoodSound::tickDelay,
                "block_search_extent", Codec.INT, MoodSound::blockSearchExtent,
                "offset", Codec.DOUBLE, MoodSound::offset,
                MoodSound::new);
    }

    public record AdditionsSound(
            SoundEvent sound,
            double tickChance
    ) {
        public static final Codec<AdditionsSound> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, AdditionsSound::sound,
                "tick_chance", Codec.DOUBLE, AdditionsSound::tickChance,
                AdditionsSound::new);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private RGBLike fogColor;
        private RGBLike skyColor;
        private RGBLike waterColor;
        private RGBLike waterFogColor;
        private RGBLike foliageColor;
        private RGBLike grassColor;
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;
        private BiomeEffects.Particle biomeParticle;
        private SoundEvent ambientSound;
        private BiomeEffects.MoodSound moodSound;
        private BiomeEffects.AdditionsSound additionsSound;
        private Music music;

        Builder() {
        }

        public Builder fogColor(RGBLike fogColor) {
            this.fogColor = fogColor;
            return this;
        }

        public Builder skyColor(RGBLike skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public Builder waterColor(RGBLike waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        public Builder waterFogColor(RGBLike waterFogColor) {
            this.waterFogColor = waterFogColor;
            return this;
        }

        public Builder foliageColor(@Nullable RGBLike foliageColor) {
            this.foliageColor = foliageColor;
            return this;
        }

        public Builder grassColor(@Nullable RGBLike grassColor) {
            this.grassColor = grassColor;
            return this;
        }

        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public Builder biomeParticle(@Nullable BiomeEffects.Particle biomeParticle) {
            this.biomeParticle = biomeParticle;
            return this;
        }

        public Builder ambientSound(@Nullable SoundEvent ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public Builder moodSound(@Nullable BiomeEffects.MoodSound moodSound) {
            this.moodSound = moodSound;
            return this;
        }

        public Builder additionsSound(@Nullable BiomeEffects.AdditionsSound additionsSound) {
            this.additionsSound = additionsSound;
            return this;
        }

        public Builder music(@Nullable Music music) {
            this.music = music;
            return this;
        }

        public BiomeEffects build() {
            Check.argCondition(fogColor == null, "fogColor is required");
            Check.argCondition(skyColor == null, "skyColor is required");
            Check.argCondition(waterColor == null, "waterColor is required");
            Check.argCondition(waterFogColor == null, "waterFogColor is required");
            Check.argCondition(grassColorModifier == null, "grassColorModifier is required");
            return new BiomeEffects(
                    fogColor, skyColor, waterColor, waterFogColor,
                    foliageColor, grassColor, grassColorModifier,
                    biomeParticle, ambientSound, moodSound,
                    additionsSound, music
            );
        }
    }
}
