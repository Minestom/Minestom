package net.minestom.server.world.biomes;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Locale;

public record BiomeEffects(int fogColor, int skyColor, int waterColor, int waterFogColor, int foliageColor,
                           int grassColor,
                           GrassColorModifier grassColorModifier, BiomeParticles biomeParticles,
                           NamespaceID ambientSound, MoodSound moodSound, AdditionsSound additionsSound,
                           Music music) {

    public static Builder builder() {
        return new Builder();
    }

    public NBTCompound toNbt() {
        NBTCompound nbt = new NBTCompound();
        nbt.setInt("fog_color", fogColor);
        if (foliageColor != -1) nbt.setInt("foliage_color", foliageColor);
        if (grassColor != -1) nbt.setInt("grass_color", grassColor);
        nbt.setInt("sky_color", skyColor);
        nbt.setInt("water_color", waterColor);
        nbt.setInt("water_fog_color", waterFogColor);
        if (grassColorModifier != null)
            nbt.setString("grass_color_modifier", grassColorModifier.name().toLowerCase(Locale.ROOT));
        if (biomeParticles != null)
            nbt.set("particle", biomeParticles.toNbt());
        if (ambientSound != null)
            nbt.setString("ambient_sound", ambientSound.toString());
        if (moodSound != null)
            nbt.set("mood_sound", moodSound.toNbt());
        if (additionsSound != null)
            nbt.set("additions_sound", additionsSound.toNbt());
        if (music != null)
            nbt.set("music", music.toNbt());
        return nbt;
    }

    public enum GrassColorModifier {
        NONE, DARK_FOREST, SWAMP;
    }

    public record MoodSound(NamespaceID sound, int tickDelay, int blockSearchExtent, double offset) {
        public @NotNull NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setInt("tick_delay", tickDelay);
            nbt.setInt("block_search_extent", blockSearchExtent);
            nbt.setDouble("offset", offset);
            return nbt;
        }
    }

    public record AdditionsSound(NamespaceID sound, double tickChance) {
        public @NotNull NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setDouble("tick_chance", tickChance);
            return nbt;
        }
    }

    public record Music(NamespaceID sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
        public @NotNull NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setInt("min_delay", minDelay);
            nbt.setInt("max_delay", maxDelay);
            nbt.setByte("replace_current_music", replaceCurrentMusic ? (byte) 1 : (byte) 0);
            return nbt;
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
        private BiomeParticles biomeParticles;
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

        public Builder biomeParticles(BiomeParticles biomeParticles) {
            this.biomeParticles = biomeParticles;
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
                    grassColor, grassColorModifier, biomeParticles,
                    ambientSound, moodSound, additionsSound, music);
        }
    }
}
