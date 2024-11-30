package net.minestom.server.world.biome;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.sound.Music;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BiomeEffects(
        @NotNull RGBLike fogColor,
        @NotNull RGBLike skyColor,
        @NotNull RGBLike waterColor,
        @NotNull RGBLike waterFogColor,
        @Nullable RGBLike foliageColor,
        @Nullable RGBLike grassColor,
        @NotNull GrassColorModifier grassColorModifier,
        @Nullable BiomeEffects.Particle biomeParticle,
        @Nullable SoundEvent ambientSound,
        @Nullable BiomeEffects.MoodSound moodSound,
        @Nullable BiomeEffects.AdditionsSound additionsSound,
        @Nullable List<WeightedMusic> music,
        @Nullable Float musicVolume
) {
    public static final BinaryTagSerializer<BiomeEffects> NBT_TYPE = BinaryTagTemplate.object(
            "fog_color", Color.NBT_TYPE, BiomeEffects::fogColor,
            "sky_color", Color.NBT_TYPE, BiomeEffects::skyColor,
            "water_color", Color.NBT_TYPE, BiomeEffects::waterColor,
            "water_fog_color", Color.NBT_TYPE, BiomeEffects::waterFogColor,
            "foliage_color", Color.NBT_TYPE.optional(), BiomeEffects::foliageColor,
            "grass_color", Color.NBT_TYPE.optional(), BiomeEffects::grassColor,
            "grass_color_modifier", GrassColorModifier.NBT_TYPE.optional(GrassColorModifier.NONE), BiomeEffects::grassColorModifier,
            "particle", Particle.NBT_TYPE.optional(), BiomeEffects::biomeParticle,
            "ambient_sound", SoundEvent.NBT_TYPE.optional(), BiomeEffects::ambientSound,
            "mood_sound", MoodSound.NBT_TYPE.optional(), BiomeEffects::moodSound,
            "additions_sound", AdditionsSound.NBT_TYPE.optional(), BiomeEffects::additionsSound,
            "music", WeightedMusic.NBT_TYPE.list().optional(), BiomeEffects::music,
            "music_volume", BinaryTagSerializer.FLOAT.optional(), BiomeEffects::musicVolume,
            BiomeEffects::new);

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;

        public static final BinaryTagSerializer<GrassColorModifier> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(GrassColorModifier.class);
    }

    public record Particle(float probability, net.minestom.server.particle.Particle particle) {
        public static final BinaryTagSerializer<Particle> NBT_TYPE = new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull BiomeEffects.Particle value) {
                return CompoundBinaryTag.builder()
                        .putFloat("probability", value.probability())
                        .put("options", value.particle().toNbt())
                        .build();
            }
        };
    }

    public record MoodSound(
            @NotNull SoundEvent sound,
            int tickDelay,
            int blockSearchExtent,
            double offset
    ) {
        public static final BinaryTagSerializer<MoodSound> NBT_TYPE = BinaryTagTemplate.object(
                "sound", SoundEvent.NBT_TYPE, MoodSound::sound,
                "tick_delay", BinaryTagSerializer.INT, MoodSound::tickDelay,
                "block_search_extent", BinaryTagSerializer.INT, MoodSound::blockSearchExtent,
                "offset", BinaryTagSerializer.DOUBLE, MoodSound::offset,
                MoodSound::new);
    }

    public record AdditionsSound(
            @NotNull SoundEvent sound,
            double tickChance
    ) {
        public static final BinaryTagSerializer<AdditionsSound> NBT_TYPE = BinaryTagTemplate.object(
                "sound", SoundEvent.NBT_TYPE, AdditionsSound::sound,
                "tick_chance", BinaryTagSerializer.DOUBLE, AdditionsSound::tickChance,
                AdditionsSound::new);
    }

    public record WeightedMusic(@NotNull Music music, int wieght) {
        public static final BinaryTagSerializer<WeightedMusic> NBT_TYPE = BinaryTagTemplate.object(
                "data", Music.NBT_TYPE, WeightedMusic::music,
                "weight", BinaryTagSerializer.INT, WeightedMusic::wieght,
                WeightedMusic::new);
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
        private List<WeightedMusic> music;
        private Float musicVolume;

        Builder() {
        }

        public Builder fogColor(@NotNull RGBLike fogColor) {
            this.fogColor = fogColor;
            return this;
        }

        public Builder skyColor(@NotNull RGBLike skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public Builder waterColor(@NotNull RGBLike waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        public Builder waterFogColor(@NotNull RGBLike waterFogColor) {
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

        public Builder grassColorModifier(@NotNull GrassColorModifier grassColorModifier) {
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

        public Builder music(@Nullable List<WeightedMusic> music) {
            this.music = music;
            return this;
        }

        public Builder musicVolume(@Nullable Float musicVolume) {
            this.musicVolume = musicVolume;
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
                    additionsSound, music, musicVolume
            );
        }
    }
}
