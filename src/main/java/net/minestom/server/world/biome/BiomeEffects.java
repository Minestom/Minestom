package net.minestom.server.world.biome;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record BiomeEffects(int fogColor, int skyColor, int waterColor, int waterFogColor, int foliageColor,
                           int grassColor,
                           GrassColorModifier grassColorModifier, BiomeParticle biomeParticle,
                           AmbientSound ambientSound, MoodSound moodSound, AdditionsSound additionsSound,
                           Music music) {

    public static Builder builder() {
        return new Builder();
    }

    public CompoundBinaryTag toNbt() {
        var builder = CompoundBinaryTag.builder();
        builder.putInt("fog_color", fogColor);
        if (foliageColor != -1)
            builder.putInt("foliage_color", foliageColor);
        if (grassColor != -1)
            builder.putInt("grass_color", grassColor);
        builder.putInt("sky_color", skyColor);
        builder.putInt("water_color", waterColor);
        builder.putInt("water_fog_color", waterFogColor);
        if (grassColorModifier != null)
            builder.putString("grass_color_modifier", grassColorModifier.name().toLowerCase(Locale.ROOT));
        if (biomeParticle != null)
            builder.put("particle", biomeParticle.toNbt());
        if (ambientSound != null)
            builder.put("ambient_sound", ambientSound.toNbt());
        if (moodSound != null)
            builder.put("mood_sound", moodSound.toNbt());
        if (additionsSound != null)
            builder.put("additions_sound", additionsSound.toNbt());
        if (music != null)
            builder.put("music", music.toNbt());
        return builder.build();
    }

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;
    }

    public record MoodSound(NamespaceID sound, int tickDelay, int blockSearchExtent, double offset) {
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .put("sound", CompoundBinaryTag.builder()
                            .putString("sound_id", sound.toString())
                            .build())
                    .putInt("tick_delay", tickDelay)
                    .putInt("block_search_extent", blockSearchExtent)
                    .putDouble("offset", offset)
                    .build();
        }
    }

    public record AdditionsSound(NamespaceID sound, double tickChance) {
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .put("sound", CompoundBinaryTag.builder()
                            .putString("sound_id", sound.toString())
                            .build())
                    .putDouble("tick_chance", tickChance)
                    .build();
        }
    }

    public record AmbientSound(NamespaceID sound, Float range) {
        public AmbientSound(NamespaceID sound) {
            this(sound, null);
        }

        public @NotNull CompoundBinaryTag toNbt() {
            var builder = CompoundBinaryTag.builder()
                    .putString("sound_id", sound.toString());

            if (range != null) builder.putFloat("range", range);
            return builder.build();
        }
    }

    public record Music(NamespaceID sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
        public @NotNull CompoundBinaryTag toNbt() {
            return CompoundBinaryTag.builder()
                    .put("sound", CompoundBinaryTag.builder()
                            .putString("sound_id", sound.toString())
                            .build())
                    .putInt("min_delay", minDelay)
                    .putInt("max_delay", maxDelay)
                    .putBoolean("replace_current_music", replaceCurrentMusic)
                    .build();
        }
    }

    public static final class Builder {
        private int fogColor;
        private int skyColor;
        private int waterColor;
        private int waterFogColor;
        private int foliageColor = -1;
        private int grassColor = -1;
        private GrassColorModifier grassColorModifier;
        private BiomeParticle biomeParticle;
        private AmbientSound ambientSound;
        private MoodSound moodSound;
        private AdditionsSound additionsSound;
        private Music music;

        Builder() {
        }

        public Builder fogColor(int fogColor) {
            this.fogColor = fogColor;
            return this;
        }

        public Builder skyColor(int skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public Builder waterColor(int waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        public Builder waterFogColor(int waterFogColor) {
            this.waterFogColor = waterFogColor;
            return this;
        }

        public Builder foliageColor(int foliageColor) {
            this.foliageColor = foliageColor;
            return this;
        }

        public Builder grassColor(int grassColor) {
            this.grassColor = grassColor;
            return this;
        }

        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public Builder biomeParticle(BiomeParticle biomeParticle) {
            this.biomeParticle = biomeParticle;
            return this;
        }

        public Builder ambientSound(AmbientSound ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public Builder moodSound(MoodSound moodSound) {
            this.moodSound = moodSound;
            return this;
        }

        public Builder additionsSound(AdditionsSound additionsSound) {
            this.additionsSound = additionsSound;
            return this;
        }

        public Builder music(Music music) {
            this.music = music;
            return this;
        }

        public BiomeEffects build() {
            return new BiomeEffects(fogColor, skyColor, waterColor, waterFogColor, foliageColor,
                    grassColor, grassColorModifier, biomeParticle,
                    ambientSound, moodSound, additionsSound, music);
        }
    }
}
