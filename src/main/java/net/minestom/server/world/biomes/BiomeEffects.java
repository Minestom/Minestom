package net.minestom.server.world.biomes;

import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class BiomeEffects {

    private final int fog_color;
    private final int sky_color;
    private final int water_color;
    private final int water_fog_color;

    private final int foliage_color;
    private final int grass_color;
    private final GrassColorModifier grass_color_modifier;
    private final BiomeParticles biomeParticles;
    private final NamespaceID ambient_sound;
    private final MoodSound mood_sound;
    private final AdditionsSound additions_sound;
    private final Music music;

    BiomeEffects(int fog_color, int sky_color, int water_color, int water_fog_color, int foliage_color, int grass_color, GrassColorModifier grass_color_modifier, BiomeParticles biomeParticles, NamespaceID ambient_sound, MoodSound mood_sound, AdditionsSound additions_sound, Music music) {
        this.fog_color = fog_color;
        this.sky_color = sky_color;
        this.water_color = water_color;
        this.water_fog_color = water_fog_color;
        this.foliage_color = foliage_color;
        this.grass_color = grass_color;
        this.grass_color_modifier = grass_color_modifier;
        this.biomeParticles = biomeParticles;
        this.ambient_sound = ambient_sound;
        this.mood_sound = mood_sound;
        this.additions_sound = additions_sound;
        this.music = music;
    }

    public static BiomeEffectsBuilder builder() {
        return new BiomeEffectsBuilder();
    }

    public NBTCompound toNbt() {
        NBTCompound nbt = new NBTCompound();
        nbt.setInt("fog_color", fog_color);
        if (foliage_color != -1)
            nbt.setInt("foliage_color", foliage_color);
        if (grass_color != -1)
            nbt.setInt("grass_color", grass_color);
        nbt.setInt("sky_color", sky_color);
        nbt.setInt("water_color", water_color);
        nbt.setInt("water_fog_color", water_fog_color);
        if (grass_color_modifier != null)
            nbt.setString("grass_color_modifier", grass_color_modifier.getType());
        if (biomeParticles != null)
            nbt.set("particle", biomeParticles.toNbt());
        if (ambient_sound != null)
            nbt.setString("ambient_sound", ambient_sound.toString());
        if (mood_sound != null)
            nbt.set("mood_sound", mood_sound.toNbt());
        if (additions_sound != null)
            nbt.set("additions_sound", additions_sound.toNbt());
        if (music != null)
            nbt.set("music", music.toNbt());
        return nbt;
    }

    public int getFog_color() {
        return this.fog_color;
    }

    public int getSky_color() {
        return this.sky_color;
    }

    public int getWater_color() {
        return this.water_color;
    }

    public int getWater_fog_color() {
        return this.water_fog_color;
    }

    public int getFoliage_color() {
        return this.foliage_color;
    }

    public int getGrass_color() {
        return this.grass_color;
    }

    public GrassColorModifier getGrass_color_modifier() {
        return this.grass_color_modifier;
    }

    public BiomeParticles getBiomeParticles() {
        return this.biomeParticles;
    }

    public NamespaceID getAmbient_sound() {
        return this.ambient_sound;
    }

    public MoodSound getMood_sound() {
        return this.mood_sound;
    }

    public AdditionsSound getAdditions_sound() {
        return this.additions_sound;
    }

    public Music getMusic() {
        return this.music;
    }

    public String toString() {
        return "BiomeEffects(fog_color=" + this.getFog_color() + ", sky_color=" +
                this.getSky_color() + ", water_color=" + this.getWater_color() + ", water_fog_color=" +
                this.getWater_fog_color() + ", foliage_color=" + this.getFoliage_color() + ", grass_color=" +
                this.getGrass_color() + ", grass_color_modifier=" + this.getGrass_color_modifier() + ", biomeParticles=" +
                this.getBiomeParticles() + ", ambient_sound=" + this.getAmbient_sound() + ", mood_sound=" +
                this.getMood_sound() + ", additions_sound=" + this.getAdditions_sound() + ", music=" + this.getMusic() + ")";
    }

    public enum GrassColorModifier {
        NONE("none"), DARK_FOREST("dark_forest"), SWAMP("swamp");

        String type;

        GrassColorModifier(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public static class MoodSound {

        private final NamespaceID sound;
        private final int tickDelay;
        private final int blockSearchExtent;
        private final double offset;

        public MoodSound(NamespaceID sound, int tickDelay, int blockSearchExtent, double offset) {
            this.sound = sound;
            this.tickDelay = tickDelay;
            this.blockSearchExtent = blockSearchExtent;
            this.offset = offset;
        }

        public NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setInt("tick_delay", tickDelay);
            nbt.setInt("block_search_extent", blockSearchExtent);
            nbt.setDouble("offset", offset);
            return nbt;
        }

    }

    public static class AdditionsSound {

        private final NamespaceID sound;
        private final double tickChance;

        public AdditionsSound(NamespaceID sound, double tickChance) {
            this.sound = sound;
            this.tickChance = tickChance;
        }

        public NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setDouble("tick_chance", tickChance);
            return nbt;
        }

    }

    public static class Music {

        private final NamespaceID sound;
        private final int minDelay;
        private final int maxDelay;
        private final boolean replaceCurrentMusic;

        public Music(NamespaceID sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
            this.sound = sound;
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
            this.replaceCurrentMusic = replaceCurrentMusic;
        }

        public NBTCompound toNbt() {
            NBTCompound nbt = new NBTCompound();
            nbt.setString("sound", sound.toString());
            nbt.setInt("min_delay", minDelay);
            nbt.setInt("max_delay", maxDelay);
            nbt.setByte("replace_current_music", replaceCurrentMusic ? (byte) 1 : (byte) 0);
            return nbt;
        }

    }

    public static class BiomeEffectsBuilder {

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

        BiomeEffectsBuilder() {
        }

        public BiomeEffects.BiomeEffectsBuilder fogColor(int fogColor) {
            this.fogColor = fogColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder skyColor(int skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder waterColor(int waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder waterFogColor(int waterFogColor) {
            this.waterFogColor = waterFogColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder foliageColor(int foliageColor) {
            this.foliageColor = foliageColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder grassColor(int grassColor) {
            this.grassColor = grassColor;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder biomeParticles(BiomeParticles biomeParticles) {
            this.biomeParticles = biomeParticles;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder ambientSound(NamespaceID ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder moodSound(MoodSound moodSound) {
            this.moodSound = moodSound;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder additionsSound(AdditionsSound additionsSound) {
            this.additionsSound = additionsSound;
            return this;
        }

        public BiomeEffects.BiomeEffectsBuilder music(Music music) {
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
