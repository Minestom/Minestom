package net.minestom.server.world.biomes;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Locale;
import java.util.Map;

public record BiomeEffects(int fogColor, int skyColor, int waterColor, int waterFogColor, int foliageColor,
                           int grassColor,
                           GrassColorModifier grassColorModifier, BiomeParticle biomeParticle,
                           NamespaceID ambientSound, MoodSound moodSound, AdditionsSound additionsSound,
                           Music music) {

    public static Builder builder() {
        return new Builder();
    }

    public NBTCompound toNbt() {
        return NBT.Compound(nbt -> {
            nbt.setInt("fog_color", fogColor);
            if (foliageColor != -1)
                nbt.setInt("foliage_color", foliageColor);
            if (grassColor != -1)
                nbt.setInt("grass_color", grassColor);
            nbt.setInt("sky_color", skyColor);
            nbt.setInt("water_color", waterColor);
            nbt.setInt("water_fog_color", waterFogColor);
            if (grassColorModifier != null)
                nbt.setString("grass_color_modifier", grassColorModifier.name().toLowerCase(Locale.ROOT));
            if (biomeParticle != null)
                nbt.set("particle", biomeParticle.toNbt());
            if (ambientSound != null)
                nbt.setString("ambient_sound", ambientSound.toString());
            if (moodSound != null)
                nbt.set("mood_sound", moodSound.toNbt());
            if (additionsSound != null)
                nbt.set("additions_sound", additionsSound.toNbt());
            if (music != null)
                nbt.set("music", music.toNbt());
        });
    }

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;
    }

    public record MoodSound(NamespaceID sound, int tickDelay, int blockSearchExtent, double offset) {
        public @NotNull NBTCompound toNbt() {
            return NBT.Compound(Map.of(
                    "sound", NBT.String(sound.toString()),
                    "tick_delay", NBT.Int(tickDelay),
                    "block_search_extent", NBT.Int(blockSearchExtent),
                    "offset", NBT.Double(offset)));
        }
    }

    public record AdditionsSound(NamespaceID sound, double tickChance) {
        public @NotNull NBTCompound toNbt() {
            return NBT.Compound(Map.of(
                    "sound", NBT.String(sound.toString()),
                    "tick_chance", NBT.Double(tickChance)));
        }
    }

    public record Music(NamespaceID sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
        public @NotNull NBTCompound toNbt() {
            return NBT.Compound(Map.of(
                    "sound", NBT.String(sound.toString()),
                    "min_delay", NBT.Int(minDelay),
                    "max_delay", NBT.Int(maxDelay),
                    "replace_current_music", NBT.Boolean(replaceCurrentMusic)));
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
        private NamespaceID ambientSound;
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

        public Builder ambientSound(NamespaceID ambientSound) {
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
